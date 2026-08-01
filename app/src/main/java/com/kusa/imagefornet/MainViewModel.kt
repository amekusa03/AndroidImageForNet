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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class MainViewModel(private val repository: SettingsRepository) : ViewModel() {

    var originalBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var processedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var watermarkText by mutableStateOf("")
        private set
    var position by mutableStateOf(WatermarkPosition.BOTTOM_RIGHT)
        private set
    var textSize by mutableStateOf(0.05f)
        private set
    var opacity by mutableStateOf(128f)
        private set
    var selectedColor by mutableStateOf(Color.White)
        private set
    var imageSize by mutableStateOf(ImageSize.ORIGINAL)
        private set
    
    var isProcessing by mutableStateOf(false)
        private set

    private var processingJob: Job? = null

    init {
        viewModelScope.launch {
            // Load initial values once
            val settings = repository.settingsFlow.first()
            watermarkText = settings.text
            position = settings.position
            textSize = settings.textSize
            opacity = settings.opacity
            selectedColor = Color(settings.color)
            imageSize = settings.imageSize
            // No need to call updateProcessedImage here as no image is loaded yet
        }
    }

    fun setImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                ImageProcessor.loadBitmap(context, uri)
            }
            originalBitmap = bitmap
            updateProcessedImage()
        }
    }

    private fun updateProcessedImage() {
        processingJob?.cancel()
        processingJob = viewModelScope.launch {
            val source = originalBitmap ?: return@launch
            isProcessing = true
            // Small delay to debounce rapid slider changes
            delay(100)
            
            val result = withContext(Dispatchers.Default) {
                val resized = ImageProcessor.resizeBitmap(source, imageSize)
                ImageProcessor.applyWatermark(
                    sourceBitmap = resized,
                    watermarkText = watermarkText,
                    position = position,
                    textColor = selectedColor.toArgb(),
                    textSizeRatio = textSize,
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

    fun updateWatermarkText(text: String) {
        watermarkText = text
        updateProcessedImage()
        viewModelScope.launch { repository.updateWatermarkText(text) }
    }

    fun updatePosition(pos: WatermarkPosition) {
        position = pos
        updateProcessedImage()
        viewModelScope.launch { repository.updatePosition(pos) }
    }

    fun updateTextSize(size: Float) {
        textSize = size
        updateProcessedImage()
        viewModelScope.launch { repository.updateTextSize(size) }
    }

    fun updateOpacity(value: Float) {
        opacity = value
        updateProcessedImage()
        viewModelScope.launch { repository.updateOpacity(value) }
    }

    fun updateColor(color: Color) {
        selectedColor = color
        updateProcessedImage()
        viewModelScope.launch { repository.updateColor(color.toArgb()) }
    }

    fun updateImageSize(size: ImageSize) {
        imageSize = size
        updateProcessedImage()
        viewModelScope.launch { repository.updateImageSize(size) }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(SettingsRepository(context.applicationContext)) as T
            }
        }
    }
}
