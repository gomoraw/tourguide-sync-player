# ツアーガイド・シンクロ動画プレイヤー (Tour Guide Sync Player)

## 1. アプリ概要

本アプリケーションは、オフライン環境（同一LAN内）で、1台のガイド端末から最大30台のユーザー端末の動画再生を同期的に制御（再生開始・一時停止）するためのAndroidアプリです。

主な目的は、ツアーや展示会などで、ガイドの説明に合わせて参加者全員のデバイスで関連動画を一斉にスタートさせる体験を提供することです。ミリ秒単位の厳密な同期よりも、「一斉に始まる」という体験の共有を重視しています。

## 2. アーキテクチャ解説

本アプリは、Googleが推奨するモダンなAndroidアプリ開発手法に準拠しています。

- **UIフレームワーク**: [Jetpack Compose](https://developer.android.com/jetpack/compose) を全面的に採用し、宣言的なUIを構築しています。
- **アーキテクチャ**: [MVVM (Model-View-ViewModel)](https://developer.android.com/jetpack/guide) パターンを採用し、UIロジックとビジネスロジックを分離しています。
- **DI (依存性注入)**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) を使用し、依存関係の管理を自動化・簡素化しています。
- **非同期処理**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) と `Flow` を活用し、非同期処理を構造化かつ効率的に記述しています。
- **ネットワーク**:
    - **サービス発見**: Android標準の [NSD (Network Service Discovery)](https://developer.android.com/training/connect-devices-wirelessly/nsd) を利用し、ユーザー端末がローカルネットワーク上のガイド端末を自動で発見します。
    - **同期通信**: [Ktor](https://ktor.io/) を使用し、ガイド側はWebSocketサーバー、ユーザー側はWebSocketクライアントとして動作します。コマンドはJSON形式で送受信されます。
- **メディア再生**: [ExoPlayer (Media3)](https://developer.android.com/guide/topics/media/media3) を使用し、動画再生を制御します。
- **ロギング**: [Timber](https://github.com/JakeWharton/Timber) を導入し、デバッグビルドとリリースビルドでログ出力を制御します。

## 3. ビルド方法

1.  本プロジェクトをAndroid Studio Giraffe | 2022.3.1 以降で開きます。
2.  必要なSDKやライブラリがGradleによって自動的にダウンロードされます。
3.  ビルドが完了したら、デバッグ用のAPKを生成できます。ターミナルで以下のコマンドを実行してください。

    ```bash
    ./gradlew assembleDebug
    ```

4.  生成されたAPKは `app/build/outputs/apk/debug/` ディレクトリにあります。

## 4. 推奨される動画フォーマット

安定した再生のため、以下のフォーマットを推奨します。動画ファイルは、ユーザー端末の `app/src/main/res/raw` ディレクトリに、`videoId` と同じファイル名（例: `video_1.mp4`）で配置することを想定しています。

-   **コンテナ**: MP4
-   **映像コーデック**: H.264/AVC Baseline Profile (BP)
-   **音声コーデック**: AAC-LC

## 5. テスト方法

### 5.1. ユニットテスト / UIテスト

プロジェクトに含まれる単体テストおよびUIテストは、Android Studioから直接実行できます。
- **ユニットテスト**: `app/src/test/` ディレクトリ内のテストクラスを実行します。
- **UIテスト**: `app/src/androidTest/` ディレクトリ内のテストクラスを実行します（エミュレータまたは実機が必要です）。

コマンドラインから全てのテストを実行する場合は、以下のコマンドを使用します。

```bash
# ユニットテスト
./gradlew testDebugUnitTest

# UIテスト
./gradlew connectedDebugAndroidTest
```

### 5.2. 結合テスト（手動）

アプリの同期機能をテストするには、最低2台のAndroidデバイス（またはエミュレータ）が必要です。

1.  両方のデバイスを**同じWi-Fiネットワーク**に接続します。
2.  1台のデバイスでアプリを起動し、「私はガイドです」を選択します。ガイド画面にPINコードが表示されます。
3.  もう1台のデバイスでアプリを起動し、「私はユーザーです」を選択します。
4.  ユーザー端末で、ガイド端末に表示されているPINコードを入力し、「接続」をタップします。
5.  ガイド端末の「接続中のユーザー」リストに、ユーザー端末が表示されることを確認します。
6.  ガイド端末で動画を選択し、再生/一時停止ボタン（FAB）を操作します。
7.  ユーザー端末の動画が、ガイドの操作に追従して再生・一時停止することを確認します。

[冬の民]