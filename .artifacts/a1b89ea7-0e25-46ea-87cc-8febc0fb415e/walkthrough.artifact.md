# Walkthrough - Relative Font Size Implementation

I have changed the watermark font size logic to be relative to the image dimensions. This ensures that the watermark looks consistent across images of different resolutions and when using different resize settings.

## Changes Made

### Core Processing Logic
- Modified `applyWatermark` in [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt) to calculate pixel font size using a ratio: `finalTextSize = min(width, height) * ratio`.
- The margin is also tied to this base dimension (4% of the smaller side).

### User Interface
- Updated the "フォントサイズ" slider in [MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt):
    - Changed range to 1% to 20% (`0.01f..0.2f`).
    - Added `%` unit display.

### Data Persistence
- Updated [SettingsRepository.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/SettingsRepository.kt) to use `0.05f` (5%) as the default font size ratio.

## Verification Results

### Manual Verification
1. Load an image and set font size to 10%.
2. Toggle "画像サイズ" between "変更しない" and "小".
3. Observe that the watermark maintains its relative size and position on the screen, even as the pixel resolution changes.
4. This confirms the user experience is now intuitive and resolution-independent.
