# Implementation Plan - Image Resizing Feature

Add an image resizing feature that allows users to select between "No change", "Small", "Medium", and "Large" sizes. The resizing will occur before applying the watermark to ensure consistent text scaling relative to the final image.

## Proposed Changes

### [Core Logic]

#### [MODIFY] [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt)
- Add `ImageSize` enum with values: `ORIGINAL`, `SMALL` (800px), `MEDIUM` (1200px), `LARGE` (1600px).
- Add `resizeBitmap` function to handle scaling while maintaining aspect ratio.
- Ensure `applyWatermark` receives the already-resized bitmap.

#### [MODIFY] [SettingsRepository.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/SettingsRepository.kt)
- Add `IMAGE_SIZE` preference key.
- Update `WatermarkSettings` data class to include `imageSize`.
- Add `updateImageSize` function to persist the selection.

### [UI & State Management]

#### [MODIFY] [MainViewModel.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainViewModel.kt)
- Add `imageSize` state.
- Update `updateProcessedImage` to resize the bitmap before applying the watermark.
- Add `updateImageSize` function.

#### [MODIFY] [MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt)
- Add UI section for "Image Size" selection using `FilterChip`s.
- Labels: "変更しない" (Original), "小" (Small), "中" (Medium), "大" (Large).

## Verification Plan

### Manual Verification
1. Open the app and select an image.
2. Change the "Image Size" setting.
3. Verify that the preview updates.
4. Verify that the watermark size remains consistent relative to the image size (e.g., if the image is smaller, the watermark shouldn't become disproportionately huge or tiny if it's based on pixel size, but since it's applied *after* resizing, it will be applied to the target resolution).
5. Save the image and check its dimensions in the gallery.
6. Share the image and verify dimensions.
