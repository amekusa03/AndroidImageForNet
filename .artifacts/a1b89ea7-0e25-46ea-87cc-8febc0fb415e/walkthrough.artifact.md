# ウォークスルー - 自動顔モザイク機能

Google ML Kit を使用して、画像内の顔を自動的に検出し、プライバシー保護のためにモザイク処理を行う機能を追加しました。

## 変更内容

### 依存関係の追加
- Google ML Kit Face Detection を導入しました。
- 非同期処理をスムーズに行うため、`kotlinx-coroutines-play-services` を追加しました。

### 画像処理ロジック ([ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt))
- `detectFaces`: ML Kit を使用してビットマップから顔の座標リストを取得します。
- `applyMosaic`: 検出された顔の領域を「縮小して拡大」する手法でピクセル化（モザイク）します。
- モザイクの強さを調整できるようにし、ユーザーが好みの粗さを選べるようにしました。

### 設定と状態管理
- `SettingsRepository.kt`: 自動モザイクの有効/無効と、モザイクの強さを永続化するようにしました。
- `MainViewModel.kt`: 画像処理パイプラインを更新し、透かしを入れる前に顔検出とモザイク処理を行うようにしました。

### ユーザーインターフェース ([MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt))
- 「プライバシー保護」セクションを新設しました。
- 自動顔モザイクのスイッチと、有効時に表示される「モザイクの強さ」スライダーを追加しました。

## 検証結果

### ビルド確認
- `app:assembleDebug` が正常に終了することを確認しました。

### 手動確認パス（推奨）
1. アプリを起動し、顔が写っている写真を選択します。
2. 「自動顔モザイク」をONにします。
3. プレビュー上で顔がモザイク処理されることを確認します。
4. スライダーを動かして、モザイクの粗さが変わることを確認します。
5. 「ギャラリーに保存」を行い、保存された画像でも正しく適用されていることを確認します。
