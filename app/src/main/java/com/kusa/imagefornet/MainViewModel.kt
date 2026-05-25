package com.kusa.imagefornet

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class MainViewModel : ViewModel() {

    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var watermarkText by mutableStateOf("Watermark")
    var position by mutableStateOf(WatermarkPosition.BOTTOM_RIGHT)
    var textSize by mutableStateOf(100f)
    var opacity by mutableStateOf(128f)
    var selectedColor by mutableStateOf(Color.White)
    
    var isProcessing by mutableStateOf(false)
        private set

    private var processingJob: Job? = null

    fun setImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageProcessor.loadBitmap(context, uri)
            }
            originalBitmap = bitmap
            updateProcessedImage()
        }
    }

    fun updateProcessedImage() {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            val source = originalBitmap ?: return@launch
            isProcessing = true
            // Small delay to debounce rapid slider changes
            delay(100)
            
            val result = withContext(Dispatchers.Default) {
                ImageProcessor.applyWatermark(
                    sourceBitmap = source,
                    watermarkText = watermarkText,
                    position = position,
                    textColor = selectedColor.toArgb(),
                    textSize = textSize,
                    opacity = opacity.toInt()
                )
            }
            processedBitmap = result
            isProcessing = false
        }
    }

    fun saveImage(context: Context, onComplete: (Boolean) -> Unit) {
        val bitmap = processedBitmap ?: return
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    saveImageToGallery(context, bitmap)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            onComplete(success)
        }
    }

    private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
        val filename = "ImageForNet_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            val fos: OutputStream? = contentResolver.openOutputStream(uri)
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
        }
    }
}
