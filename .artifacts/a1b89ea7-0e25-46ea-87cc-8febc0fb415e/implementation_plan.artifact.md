# Implementation Plan - Relative Font Size

Change the watermark font size to be relative to the image dimensions instead of a fixed pixel size. This ensures the watermark remains visually consistent regardless of the source image resolution or selected resize option.

## Proposed Changes

### [Core Logic]

#### [MODIFY] [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt)
- Update `applyWatermark` to treat `textSize` as a ratio (e.g., 0.05 for 5%) of the image's smaller dimension.
- Calculate the actual pixel size: `finalTextSize = min(width, height) * textSize`.

### [UI & State Management]

#### [MODIFY] [MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt)
- Update `SliderControl` for "フォントサイズ":
    - Set `valueRange` to a percentage-friendly range (e.g., `0.01f..0.2f`).
    - Update `displayMultiplier` to 100 and add `%` unit to show it as a percentage (1% to 20%).

#### [MODIFY] [SettingsRepository.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/SettingsRepository.kt)
- Update default `TEXT_SIZE` to a reasonable ratio, like `0.05f` (5%).

#### [MODIFY] [MainViewModel.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainViewModel.kt)
- Update initial `textSize` state to `0.05f`.

## Verification Plan

### Manual Verification
1. Load an image.
2. Adjust the font size slider (verify it shows 1% to 20%).
3. Change "Image Size" (e.g., from Original to Small).
4. Verify that the watermark's visual size relative to the entire image remains the same.
5. Save the image and check results.
