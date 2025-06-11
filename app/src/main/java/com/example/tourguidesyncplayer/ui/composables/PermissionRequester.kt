package com.example.tourguidesyncplayer.ui.composables

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.tourguidesyncplayer.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.POST_NOTIFICATIONS
    )
} else {
    listOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequester(
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberMultiplePermissionsState(permissions = requiredPermissions)

    val openSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permissionState.launchMultiplePermissionRequest()
    }

    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        LaunchedEffect(permissionState) {
            val isPermanentlyDeclined = !permissionState.shouldShowRationale && permissionState.revokedPermissions.isNotEmpty()
            if (isPermanentlyDeclined) {
                launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.permission_permanently_denied_message),
                        actionLabel = context.getString(R.string.permission_button_open_settings),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        openSettingsLauncher.launch(intent)
                    }
                }
            } else {
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }
}

