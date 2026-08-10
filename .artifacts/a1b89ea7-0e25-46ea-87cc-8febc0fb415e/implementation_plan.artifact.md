# 実装計画 - 低スペックデバイス向けメモリ最適化とクラッシュ修正

低スペックデバイスでスライダーを操作した際に発生する `OutOfMemoryError` (OOM) を修正し、アプリの安定性を向上させます。

## 原因分析
Logcat の解析結果から、以下の原因が特定されました：
1. **メモリ不足**: 画像処理中にビットマップのコピーが複数作成され、ヒープメモリ（約96MB）の上限を超えている。
2. **キャンセルの不徹底**: 前の処理が完了する前に新しい処理が始まっても、バックグラウンドでのメモリ確保（`Bitmap.copy` 等）が継続している可能性がある。

## 解決策の概要

### 1. マニフェスト設定の変更
- `android:largeHeap="true"` を有効にし、画像処理に必要なメモリ空間を確保します。

### 2. ビットマップ処理の最適化
- **インプレース加工**: 処理の各ステップ（モザイク、透かし）で毎回コピーを作成するのではなく、一度作成した作業用ビットマップを再利用するように変更します。
- **中間ビットマップの明示的リサイクル**: 不要になった中間ビットマップを `recycle()` で即座に解放します。

### 3. コルーチンのキャンセル対応強化
- 重い処理の合間に `ensureActive()` を呼び出し、新しい操作によってキャンセルされた場合は即座に中断するようにします。

## 変更内容の提案

### [マニフェスト]

#### [MODIFY] [AndroidManifest.xml](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/AndroidManifest.xml)
- `<application>` タグに `android:largeHeap="true"` を追加。

### [コアロジック]

#### [MODIFY] [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt)
- `applyMosaic` と `applyWatermark` を更新し、既存のビットマップを直接加工するか、新しいコピーを作成するかを選択できるように変更（または、パイプラインの最初でのみコピーを作成する設計に変更）。
- ループ内や重い処理の前に `yield()` または `isActive` チェックを追加（呼び出し元からコルーチンコンテキストを引き継ぐ場合）。

### [状態管理]

#### [MODIFY] [MainViewModel.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainViewModel.kt)
- 画像処理パイプラインのメモリ管理を強化。
- 新しい `processedBitmap` が生成された際、古い `processedBitmap` が不要であれば安全に処理できるように検討（Composeのライフサイクルに注意）。

## 検証計画

### 手動確認
1. メモリの少ないデバイス（またはエミュレータ）で、高解像度の画像を読み込む。
2. スライダー（フォントサイズ、モザイク強度等）を激しく動かす。
3. クラッシュが発生せず、プレビューが追従することを確認する。
4. `android:largeHeap` の効果により、メモリ使用量に余裕があるか確認する。
