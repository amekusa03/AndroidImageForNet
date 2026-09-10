package com.kusa.imagefornet

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kusa.imagefornet.ui.theme.ImageForNetTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageForNetTheme {
                ImageForNetApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageForNetApp() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel(factory = MainViewModel.provideFactory(context))
    val scrollState = rememberScrollState()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setImage(context, it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ImageForNet", fontWeight = FontWeight.Bold) },
                actions = {
                    if (viewModel.processedBitmap != null) {
                        IconButton(onClick = { 
                            shareImage(context, viewModel.processedBitmap!!)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            if (viewModel.originalBitmap == null) {
                ExtendedFloatingActionButton(
                    onClick = { launcher.launch("image/*") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.select_image)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = viewModel.processedBitmap ?: viewModel.originalBitmap, label = "preview") { bitmap ->
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.please_select_image),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                if (viewModel.isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            }

            AnimatedVisibility(
                visible = viewModel.originalBitmap != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Controls Card
                    ElevatedCard(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.watermarkText,
                                onValueChange = { 
                                    viewModel.updateWatermarkText(it)
                                },
                                label = { Text(stringResource(R.string.watermark_text_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Text(stringResource(R.string.position_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            PositionGrid(
                                selectedPosition = viewModel.position,
                                onPositionSelected = { 
                                    viewModel.updatePosition(it)
                                }
                            )

                            Text(stringResource(R.string.color_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            ColorPalette(
                                selectedColor = viewModel.selectedColor,
                                onColorSelected = { 
                                    viewModel.updateColor(it)
                                }
                            )

                            SliderControl(
                                label = stringResource(R.string.font_size_label),
                                value = viewModel.textSize,
                                onValueChange = { 
                                    viewModel.updateTextSize(it)
                                },
                                valueRange = 0.01f..0.2f,
                                displayMultiplier = 100f,
                                unit = "%"
                            )

                            SliderControl(
                                label = stringResource(R.string.opacity_label),
                                value = viewModel.opacity,
                                onValueChange = { 
                                    viewModel.updateOpacity(it)
                                },
                                valueRange = 0f..255f,
                                displayMultiplier = 100f / 255f,
                                unit = "%"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            Text(stringResource(R.string.image_size_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            ImageSizeSelector(
                                selectedSize = viewModel.imageSize,
                                onSizeSelected = { viewModel.updateImageSize(it) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                            Text(stringResource(R.string.privacy_protection_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.auto_mosaic_label), style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = viewModel.isAutoMosaicEnabled,
                                    onCheckedChange = { viewModel.updateAutoMosaicEnabled(it) }
                                )
                            }

                            if (viewModel.isAutoMosaicEnabled) {
                                SliderControl(
                                    label = stringResource(R.string.mosaic_strength_label),
                                    value = viewModel.mosaicStrength,
                                    onValueChange = { viewModel.updateMosaicStrength(it) },
                                    valueRange = 0f..1f,
                                    displayMultiplier = 100f,
                                    unit = "%"
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveImage(context) { success ->
                                val msg = if (success) {
                                    context.getString(R.string.saved_to_gallery)
                                } else {
                                    context.getString(R.string.failed_to_save)
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !viewModel.isProcessing
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.save_to_gallery), fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.select_another_image))
                    }
                }
            }
        }
    }
}

@Composable
fun ImageSizeSelector(selectedSize: ImageSize, onSizeSelected: (ImageSize) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ImageSize.values().forEach { size ->
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selectedSize == size,
                onClick = { onSizeSelected(size) },
                label = { Text(stringResource(size.labelRes), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
fun PositionGrid(selectedPosition: WatermarkPosition, onPositionSelected: (WatermarkPosition) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val rows = listOf(
            listOf(WatermarkPosition.TOP_LEFT, WatermarkPosition.TOP_RIGHT),
            listOf(WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.BOTTOM_RIGHT)
        )
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { pos ->
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = selectedPosition == pos,
                        onClick = { onPositionSelected(pos) },
                        label = { Text(stringResource(pos.labelRes), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPalette(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val colors = listOf(Color.White, Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Cyan, Color.Magenta)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (selectedColor == color) 2.dp else 1.dp,
                        color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == color) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(if (color == Color.White) Color.Black else Color.White))
                }
            }
        }
    }
}

@Composable
fun SliderControl(label: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, displayMultiplier: Float = 1f, unit: String = "") {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${(value * displayMultiplier).toInt()}$unit", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

private fun shareImage(context: android.content.Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "shared_image.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_image)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.failed_to_share), Toast.LENGTH_SHORT).show()
    }
}

