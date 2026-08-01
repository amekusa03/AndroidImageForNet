# Walkthrough - Image Resizing Feature

I have added an image resizing feature that allows you to scale down images before applying the watermark. This ensures that the watermark text size remains consistent relative to the final output image.

## Changes Made

### Image Processing Logic
- Added `ImageSize` enum in [ImageProcessor.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/ImageProcessor.kt) with options:
    - **変更しない (Original)**: No resizing.
    - **小 (Small)**: Max side 800px.
    - **中 (Medium)**: Max side 1200px.
    - **大 (Large)**: Max side 1600px.
- Implemented `resizeBitmap` to perform aspect-ratio-aware scaling.

### State and Persistence
- Added `imageSize` to `WatermarkSettings` and `SettingsRepository.kt` to persist the user's preference.
- Updated `MainViewModel.kt` to chain the resizing process before the watermarking process.

### User Interface
- Added an "画像サイズ" (Image Size) selection section in [MainActivity.kt](file:///media/data/Users/ame/document/AndroidStudioProjects/ImageForNet/app/src/main/java/com/kusa/imagefornet/MainActivity.kt) using `FilterChip` components.

## Verification Results

### Automated Tests
- Compiled the project to ensure no syntax errors.

### Manual Verification Path
1. Launch the app.
2. Select an image from the gallery.
3. Observe the "画像サイズ" selection below the sliders.
4. Select "小" or "中".
5. Notice the preview might slightly adjust if the aspect ratio calculation triggers a resize.
6. Save the image and verify that the output file dimensions match the selected maximum side limit.
7. Verify that the watermark text size (e.g., 100px) looks identical across different resize options because it's applied after the image has been scaled to its target resolution.
