# 実装計画 - 自動顔モザイク機能

ML Kit Face Detection を使用して、画像内の顔を自動的に検出し、プライバシー保護のためにモザイク処理（ピクセル化）を行う機能を追加します。

## ユーザー確認事項

> [!IMPORTANT]
> この機能には Google ML Kit Face Detection 依存関係の追加が必要です。これにより、アプリのバイナリサイズが増加し、実行には Google Play 開発者サービスが必要になります（設定によりモデルをアプリに同梱することも可能です）。今回の実装では、簡便さと確実性のために「バンドル版」を使用します。

## 変更内容の提案

### [依存関係]

#### [MODIFY] [libs.versions.toml](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/gradle/libs.versions.toml)
- `[versions]` に `mlkit-face-detection = "16.1.7"` を追加。
- `[libraries]` に `mlkit-face-detection = { group = "com.google.mlkit", name = "face-detection", version.ref = "mlkit-face-detection" }` を追加。

#### [MODIFY] [app/build.gradle.kts](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/build.gradle.kts)
- `dependencies` に `implementation(libs.mlkit.face.detection)` を追加。

### [コアロジック]

#### [MODIFY] [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt)
- ML Kit を使用した顔検出関数 `detectFaces(bitmap: Bitmap): List<Face>` を追加。
- モザイク処理関数 `applyMosaic(bitmap: Bitmap, faces: List<Face>, strength: Float): Bitmap` を追加。
- モザイクの仕組み：
    - 各顔の境界ボックス（Bounding Box）に対して、`strength` に応じて縮小（例: 1/10〜1/50）。
    - バイリニアフィルタリングを無効にして元のサイズに拡大し、ピクセル化された外観を作成。
    - ピクセル化された顔を元のビットマップにオーバーレイ。

### [設定と状態管理]

#### [MODIFY] [SettingsRepository.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/SettingsRepository.kt)
- `AUTO_MOSAIC_ENABLED` (boolean) と `MOSAIC_STRENGTH` (float) の設定キーを追加。
- `WatermarkSettings` データクラスと永続化メソッドを更新。

#### [MODIFY] [MainViewModel.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainViewModel.kt)
- `isAutoMosaicEnabled` と `mosaicStrength` の状態変数を追加。
- `updateProcessedImage` の画像処理パイプラインに顔検出とモザイク処理のステップを組み込む。
- パイプライン順序：`リサイズ -> 顔検出 -> モザイク適用 -> 透かし適用`。

### [ユーザーインターフェース]

#### [MODIFY] [MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt)
- 設定カードに「プライバシー保護」セクションを追加。
- 「自動顔モザイク」の `Switch` を追加。
- 「モザイクの強さ」の `SliderControl` を追加（スイッチがONの場合のみ表示）。

## 検証計画

### 自動テスト
- プロジェクトをビルドして、依存関係の統合を確認。

### 手動確認
1. 1つ以上の顔が含まれる写真を読み込む。
2. 「自動顔モザイク」スイッチをONにする。
3. プレビューで顔が自動的にモザイク処理されることを確認。
4. 「モザイクの強さ」を調整し、ピクセルの大きさが変わることを確認。
5. 画像サイズを変更し、リサイズ後の画像に対しても顔検出とモザイクが正しく機能することを確認。
6. 画像を保存・共有し、出力結果を確認。
