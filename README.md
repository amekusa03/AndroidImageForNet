# ImageForNet

ImageForNet は、インターネットへの画像投稿前にプライバシー保護（EXIF情報の削除）とウォーターマーク（透かし）の追加を簡単に行うためのAndroidアプリです。
Android 6.0 (API 23) 以上対応。

## 主な機能

- **EXIF情報の削除**: 画像に含まれるGPS位置情報、撮影日時、カメラのモデル名などのメタデータを完全に削除します。
- **リアルタイム・ウォーターマーク**:
    - 自由なテキストを設定可能。
    - 配置場所の選択（左上、右上、左下、右下）。
    - 文字色、サイズ、不透明度のカスタマイズ。
    - パラメータ変更を即座にプレビューに反映。
- **共有機能**: 加工した画像をそのまま他のアプリ（SNS等）へ共有可能。
- **シンプルな操作性**: ギャラリーから画像を選択し、直感的なUIで加工して保存するだけのスムーズなフロー。

## 技術スタック (Modern Android Stack)

- **言語**: Kotlin
- **アーキテクチャ**: MVVM (ViewModel + State)
- **UI**: Jetpack Compose / Material Design 3
- **非同期処理**: Kotlin Coroutines
- **主なライブラリ**:
    - `androidx.exifinterface`: メタデータの確実な処理。
    - `androidx.lifecycle:lifecycle-viewmodel-compose`: Composeにおけるアーキテクチャ支援。
    - `Bitmap API`: 画像の合成とレンダリング。

## セットアップ

1. Android Studio でプロジェクトを開きます。
2. `gradle.properties` や `build.gradle.kts` が正しく同期されていることを確認します。
3. デバイスまたはエミュレータで実行します。

## ライセンス

[Your License - e.g., MIT]
