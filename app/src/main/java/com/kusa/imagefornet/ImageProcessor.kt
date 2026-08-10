package com.kusa.imagefornet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.yield
import kotlinx.coroutines.tasks.await

enum class WatermarkPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

enum class ImageSize(val label: String, val maxSide: Int?) {
    ORIGINAL("変更しない", null),
    SMALL("小", 800),
    MEDIUM("中", 1200),
    LARGE("大", 1600)
}

object ImageProcessor {

    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        
        // 1. Read orientation from EXIF before it's lost
        val exif = try {
            val exifStream = context.contentResolver.openInputStream(uri)
            val e = exifStream?.use { ExifInterface(it) }
            e
        } catch (e: Exception) {
            null
        }

        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        ) ?: ExifInterface.ORIENTATION_NORMAL

        // 2. Decode the bitmap
        val bitmap = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null

        // 3. Rotate if necessary
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun resizeBitmap(source: Bitmap, targetSize: ImageSize): Bitmap {
        val maxSide = targetSize.maxSide ?: return source
        
        val width = source.width
        val height = source.height
        
        val scale = if (width > height) {
            if (width <= maxSide) return source
            maxSide.toFloat() / width
        } else {
            if (height <= maxSide) return source
            maxSide.toFloat() / height
        }
        
        val targetWidth = (width * scale).toInt()
        val targetHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    suspend fun detectFaces(bitmap: Bitmap): List<Rect> {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val detector = FaceDetection.getClient(options)
        
        // Optimize: Use a smaller version of the bitmap for detection if it's too large
        val maxDetectionSide = 480
        val scale = if (bitmap.width > maxDetectionSide || bitmap.height > maxDetectionSide) {
            maxDetectionSide.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else {
            1.0f
        }

        val detectionBitmap = if (scale != 1.0f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val image = InputImage.fromBitmap(detectionBitmap, 0)
        return try {
            val faces = detector.process(image).await()
            faces.map { face ->
                val box = face.boundingBox
                if (scale != 1.0f) {
                    val invScale = 1.0f / scale
                    Rect(
                        (box.left * invScale).toInt(),
                        (box.top * invScale).toInt(),
                        (box.right * invScale).toInt(),
                        (box.bottom * invScale).toInt()
                    )
                } else {
                    box
                }
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            if (detectionBitmap != bitmap) {
                detectionBitmap.recycle()
            }
            detector.close()
        }
    }

    suspend fun applyMosaic(bitmap: Bitmap, faces: List<Rect>, strength: Float) {
        if (faces.isEmpty()) return
        
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = false }

        for (rect in faces) {
            yield() // Check for cancellation
            val left = rect.left.coerceAtLeast(0)
            val top = rect.top.coerceAtLeast(0)
            val right = rect.right.coerceAtMost(bitmap.width)
            val bottom = rect.bottom.coerceAtMost(bitmap.height)
            val width = right - left
            val height = bottom - top

            if (width <= 0 || height <= 0) continue

            // strength: 0.0f (weak) to 1.0f (strong)
            // factor: 2 (min) to 52 (max)
            val factor = (strength * 50 + 2).toInt()
            val smallWidth = (width / factor).coerceAtLeast(1)
            val smallHeight = (height / factor).coerceAtLeast(1)

            val faceBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
            val smallBitmap = Bitmap.createScaledBitmap(faceBitmap, smallWidth, smallHeight, false)
            val pixelatedBitmap = Bitmap.createScaledBitmap(smallBitmap, width, height, false)

            canvas.drawBitmap(pixelatedBitmap, left.toFloat(), top.toFloat(), paint)
            
            faceBitmap.recycle()
            smallBitmap.recycle()
            pixelatedBitmap.recycle()
        }
    }

    fun applyWatermark(
        targetBitmap: Bitmap,
        watermarkText: String,
        position: WatermarkPosition,
        textColor: Int,
        textSizeRatio: Float,
        opacity: Int // 0-255
    ) {
        val canvas = Canvas(targetBitmap)
        
        // Calculate actual pixel size based on the smaller dimension
        val baseDimension = targetBitmap.width.coerceAtMost(targetBitmap.height)
        val calculatedTextSize = baseDimension * textSizeRatio
        
        val paint = Paint().apply {
            color = textColor
            alpha = opacity
            this.textSize = calculatedTextSize
            isAntiAlias = true
            textAlign = when (position) {
                WatermarkPosition.TOP_LEFT, WatermarkPosition.BOTTOM_LEFT -> Paint.Align.LEFT
                WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> Paint.Align.RIGHT
            }
        }

        // Use responsive margin
        val margin = baseDimension * 0.04f
        
        val x = when (position) {
            WatermarkPosition.TOP_LEFT, WatermarkPosition.BOTTOM_LEFT -> margin
            WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> targetBitmap.width - margin
        }
        
        val y = when (position) {
            WatermarkPosition.TOP_LEFT, WatermarkPosition.TOP_RIGHT -> paint.textSize + margin
            WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.BOTTOM_RIGHT -> targetBitmap.height - margin
        }

        canvas.drawText(watermarkText, x, y, paint)
    }
}
