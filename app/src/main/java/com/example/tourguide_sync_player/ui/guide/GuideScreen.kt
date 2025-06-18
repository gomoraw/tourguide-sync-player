// GuideScreen.kt 【修正案】

package com.example.tourguide_sync_player.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    navController: NavController,
    viewModel: GuideViewModel = hiltViewModel() // ViewModelを注入
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // UI Stateを監視

    var selectedVideo by remember { mutableStateOf<String?>(null) }
    val isPlaying = uiState.syncState.isPlaying // 再生状態をUI Stateから取得

    // サンプル動画リスト
    val videoList = remember {
        (1..20).map { "動画 $it" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ガイド画面",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedVideo != null) {
                FloatingActionButton(
                    onClick = { viewModel.onPlayPauseClick() }, // ViewModelの関数を呼び出す
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "停止" else "再生",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ★接続状態表示をUI Stateから取得した値で更新
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "接続中のユーザー: ${uiState.connectedClients} 台",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "PINコード: 1234",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // （以降のコードは大きな変更なし）
            // ...
        }
    }
}