package com.example.tourguidesyncplayer.ui.guide

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// 修正箇所: import文を追加
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.tourguidesyncplayer.R
import com.example.tourguidesyncplayer.data.model.ConnectedUser
import com.example.tourguidesyncplayer.data.model.ConnectionStatus
import com.example.tourguidesyncplayer.data.model.GuideUiState
import com.example.tourguidesyncplayer.data.model.VideoContent
import com.example.tourguidesyncplayer.ui.composables.PermissionRequester
import com.example.tourguidesyncplayer.ui.theme.TourGuideSyncPlayerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GuideScreen(
    navController: NavController,
    viewModel: GuideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { GuideTopAppBar(uiState) },
        floatingActionButton = {
            GuideFAB(
                isPlaying = uiState.isPlaying,
                onClick = { viewModel.onPlayPauseClicked() },
                isEnabled = uiState.selectedVideoId != null
            )
        }
    ) { paddingValues ->
        PermissionRequester(snackbarHostState = snackbarHostState) {
            GuideContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState,
                onVideoSelected = { videoId -> viewModel.onVideoSelected(videoId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuideTopAppBar(uiState: GuideUiState) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(
                    id = R.string.guide_top_bar_title,
                    uiState.connectedUsers.size
                )
            )
        },
        actions = {
            if (uiState.isServiceRunning) {
                Text(
                    text = stringResource(id = R.string.guide_top_bar_pin, uiState.pinCode),
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun GuideFAB(isPlaying: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    var fabEnabled by remember { mutableStateOf(isEnabled) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isEnabled) {
        fabEnabled = isEnabled
    }

    FloatingActionButton(
        onClick = {
            if (fabEnabled) {
                onClick()
                fabEnabled = false
                scope.launch {
                    delay(1000)
                    fabEnabled = true
                }
            }
        },
        containerColor = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun GuideContent(
    modifier: Modifier = Modifier,
    uiState: GuideUiState,
    onVideoSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(id = R.string.guide_video_list_header),
                style = MaterialTheme.typography.titleLarge
            )
        }
        items(uiState.videoList) { video ->
            VideoItem(
                video = video,
                isSelected = video.id == uiState.selectedVideoId,
                onSelected = { onVideoSelected(video.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = stringResource(id = R.string.guide_connected_users_header),
                style = MaterialTheme.typography.titleLarge
            )
        }
        if (uiState.connectedUsers.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.guide_no_users_connected),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.connectedUsers, key = { it.id }) { user ->
                UserItem(user = user)
            }
        }
    }
}

@Composable
private fun VideoItem(
    video: VideoContent,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = video.title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun UserItem(user: ConnectedUser) {
    val statusColor = when (user.status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50) // Green
        ConnectionStatus.DELAYED -> Color(0xFFFFC107) // Yellow
        ConnectionStatus.DISCONNECTED -> Color(0xFFF44336) // Red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = statusColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = user.deviceName, style = MaterialTheme.typography.bodyLarge)
    }
}


@Preview(showBackground = true)
@Composable
fun GuideScreenPreview() {
    val previewState = GuideUiState(
        isServiceRunning = true,
        pinCode = "1234",
        connectedUsers = listOf(
            ConnectedUser("1", "Pixel 8 Pro", status = ConnectionStatus.CONNECTED),
            ConnectedUser("2", "Galaxy S23", status = ConnectionStatus.DELAYED),
            ConnectedUser("3", "Xperia 1 V", status = ConnectionStatus.DISCONNECTED)
        ),
        videoList = listOf(
            VideoContent("v1", "オープニング", ""),
            VideoContent("v2", "歴史", "")
        ),
        selectedVideoId = "v1",
        isPlaying = true
    )
    TourGuideSyncPlayerTheme {
        Scaffold(
            topBar = { GuideTopAppBar(uiState = previewState) },
            floatingActionButton = {
                GuideFAB(
                    isPlaying = previewState.isPlaying,
                    isEnabled = previewState.selectedVideoId != null,
                    onClick = {}
                )
            }
        ) { padding ->
            GuideContent(
                modifier = Modifier.padding(padding),
                uiState = previewState,
                onVideoSelected = {}
            )
        }
    }
}