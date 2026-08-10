# ウォークスルー - メモリ最適化とクラッシュ修正

低スペックデバイスで発生していた `OutOfMemoryError` を解決し、激しいスライダー操作時でもアプリが安定して動作するように改善しました。

## 実施した主な変更

### 1. メモリ上限の拡大
- [AndroidManifest.xml](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/AndroidManifest.xml) に `android:largeHeap="true"` を追加しました。これにより、画像処理に必要なメモリ空間をより柔軟に確保できるようになりました。

### 2. ビットマップコピーの削減 ([ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt))
- これまで各ステップ（モザイク、透かし）で作成していたビットマップのコピーを廃止し、**一つの作業用ビットマップに対して直接上書き（インプレース）で加工**するように変更しました。
- これにより、メモリ消費量を大幅に削減しました。

### 3. キャンセル対応の強化 ([MainViewModel.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainViewModel.kt))
- スライダー操作によって新しい処理が開始された際、古い処理を即座に中断し、確保していたメモリ（作業用ビットマップ）を `recycle()` で解放するようにしました。
- `isActive` チェックや `yield()` を各所に配置し、コルーチンのキャンセルに対する応答性を高めました。

## 検証結果

### 動作確認
- 連続したスライダー操作を行っても、メモリが飽和することなく、クラッシュが発生しないことを確認しました。
- `largeHeap` の適用により、高解像度の画像でも安定して処理が行えるようになりました。
