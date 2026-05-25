package com.kusa.imagefornet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

enum class WatermarkPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
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

    fun applyWatermark(
        sourceBitmap: Bitmap,
        watermarkText: String,
        position: WatermarkPosition,
        textColor: Int,
        textSize: Float,
        opacity: Int // 0-255
    ): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        
        val paint = Paint().apply {
            color = textColor
            alpha = opacity
            this.textSize = textSize
            isAntiAlias = true
            textAlign = when (position) {
                WatermarkPosition.TOP_LEFT, WatermarkPosition.BOTTOM_LEFT -> Paint.Align.LEFT
                WatermarkPosition.TOP_RIGHT, WatermarkPosition.BOTTOM_RIGHT -> Paint.Align.RIGHT
            }
        }

        // Use responsive margin
        val margin = resultBitmap.width.coerceAtMost(resultBitmap.height) * 0.04f
        
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
