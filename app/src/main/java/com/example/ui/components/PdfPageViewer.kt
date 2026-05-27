package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfPageViewer(pdfFile: File, modifier: Modifier = Modifier) {
    var bitmapState by remember(pdfFile) { mutableStateOf<Bitmap?>(null) }
    var errorState by remember(pdfFile) { mutableStateOf<String?>(null) }
    var isLoading by remember(pdfFile) { mutableStateOf(true) }

    LaunchedEffect(pdfFile) {
        isLoading = true
        errorState = null
        try {
            withContext(Dispatchers.IO) {
                if (!pdfFile.exists()) {
                    errorState = "PDF file on disk is not generated yet."
                    isLoading = false
                    return@withContext
                }
                val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    // Scale A4 beautifully (double resolution for visual crispness)
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    
                    // Pre-fill with premium clean canvas white color
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmapState = bitmap
                } else {
                    errorState = "The PDF contains no pages."
                }
                renderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorState = "PDF Render Error: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.707f) // Standard A4 Aspect Ratio (1 : sqrt(2))
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            errorState != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = errorState ?: "Unknown Rendering Error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            bitmapState != null -> {
                Image(
                    bitmap = bitmapState!!.asImageBitmap(),
                    contentDescription = "Rendered Local PDF",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
