package com.kusa.imagefornet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
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

    suspend fun detectFaces(bitmap: Bitmap): List<Face> {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val detector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        return try {
            detector.process(image).await()
        } catch (e: Exception) {
            emptyList()
        } finally {
            detector.close()
        }
    }

    fun applyMosaic(bitmap: Bitmap, faces: List<Face>, strength: Float): Bitmap {
        if (faces.isEmpty()) return bitmap
        
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = false }

        for (face in faces) {
            val bounds = face.boundingBox
            val left = bounds.left.coerceAtLeast(0)
            val top = bounds.top.coerceAtLeast(0)
            val right = bounds.right.coerceAtMost(bitmap.width)
            val bottom = bounds.bottom.coerceAtMost(bitmap.height)
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
        return result
    }

    fun applyWatermark(
        sourceBitmap: Bitmap,
        watermarkText: String,
        position: WatermarkPosition,
        textColor: Int,
        textSizeRatio: Float,
        opacity: Int // 0-255
    ): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        
        // Calculate actual pixel size based on the smaller dimension
        val baseDimension = resultBitmap.width.coerceAtMost(resultBitmap.height)
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
            WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> resultBitmap.width - margin
        }
        
        val y = when (position) {
            WatermarkPosition.TOP_LEFT, WatermarkPosition.TOP_RIGHT -> paint.textSize + margin
            WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.BOTTOM_RIGHT -> resultBitmap.height - margin
        }

        canvas.drawText(watermarkText, x, y, paint)
        
        return resultBitmap
    }
}
