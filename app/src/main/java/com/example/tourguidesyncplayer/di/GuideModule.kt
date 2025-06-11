package com.example.tourguidesyncplayer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

// ViewModelスコープで提供するモジュールを別途作成
@Module
@InstallIn(ViewModelComponent::class)
object GuideModule {
    // ViewModelがServerCallbackを実装するため、ここでは単純なProvidesは行わない。
    // ViewModel内で直接インスタンス化するアプローチを採用しているため、このファイルは空でよい。
}

