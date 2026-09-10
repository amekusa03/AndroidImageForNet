# ImageForNet

Language: [English](README.md) | [日本語](README.ja.md)

ImageForNet は、インターネットへの画像投稿前にプライバシー保護（顔のモザイク、EXIF情報の削除）と、画像のリサイズ、ウォーターマーク（透かし）の追加を簡単に行うためのAndroidアプリです。
Android 6.0 (API 23) 以上対応。

## 主な機能

- **自動顔モザイク (Privacy Protection)**:
    - Google ML Kit を使用したAIによる顔の自動検知。
    - 検出された顔に即座にモザイク（ピクセル化）を適用。
    - モザイクの強さを自由に調整可能。
- **EXIF情報の削除**:
    - 画像に含まれるGPS位置情報、撮影日時、カメラのモデル名などのメタデータを完全に削除します。
- **画像のリサイズ**:
    - 用途に合わせて画像サイズ（小・中・大・オリジナル）を選択可能。
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
    - `Google ML Kit Face Detection`: デバイス上での高速な顔検出.
    - `androidx.exifinterface`: メタデータの確実な処理。
    - `androidx.datastore`: 設定の永続化。
    - `androidx.lifecycle:lifecycle-viewmodel-compose`: Composeにおけるアーキテクチャ支援。
    - `Bitmap API`: 画像の加工・合成とレンダリング。

## セットアップ

1. Android Studio でプロジェクトを開きます。
2. `gradle.properties` や `build.gradle.kts` が正しく同期されていることを確認します。
3. デバイスまたはエミュレータで実行します。

## ライセンス

[Your License - e.g., MIT]
