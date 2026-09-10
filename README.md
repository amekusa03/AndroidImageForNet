# ImageForNet

Language: [English](README.md) | [日本語](README.ja.md)

ImageForNet is an Android application designed to easily protect your privacy before posting images to the internet by removing EXIF metadata, blurring/mosaicing faces automatically, resizing images, and adding customizable watermarks.
Supports Android 6.0 (API 23) and above.

## Features

- **Automatic Face Mosaic (Privacy Protection)**:
    - AI-powered face detection using Google ML Kit.
    - Instantly applies a pixelation effect (mosaic) to detected faces on-device.
    - Adjustable mosaic strength.
- **EXIF Metadata Removal**:
    - Completely strips metadata including GPS location, date/time taken, camera model, etc.
- **Image Resizing**:
    - Select suitable image dimensions (Small, Medium, Large, or Original).
- **Real-time Watermarking**:
    - Custom text input.
    - Select position (Top-Left, Top-Right, Bottom-Left, Bottom-Right).
    - Customize font color, size, and opacity.
    - Instant live preview as parameter values change.
- **Easy Sharing**: Share processed images directly to other apps (social media, messaging, etc.).
- **Simple & Intuitive UX**: Select an image from your gallery, adjust options via an intuitive UI, and save or share smoothly.

## Tech Stack (Modern Android Stack)

- **Language**: Kotlin
- **Architecture**: MVVM (ViewModel + State)
- **UI**: Jetpack Compose / Material Design 3
- **Asynchronous Operations**: Kotlin Coroutines
- **Key Libraries**:
    - `Google ML Kit Face Detection`: Fast, on-device face detection.
    - `androidx.exifinterface`: Reliable EXIF metadata handling.
    - `androidx.datastore`: Settings persistence.
    - `androidx.lifecycle:lifecycle-viewmodel-compose`: Architecture support for Compose.
    - `Bitmap API`: Image processing, blending, and rendering.

## Setup & Installation

1. Open the project in Android Studio.
2. Ensure Gradle sync completes successfully (`gradle.properties` / `build.gradle.kts`).
3. Build and run on a physical device or emulator.

## License

[Your License - e.g., MIT]
