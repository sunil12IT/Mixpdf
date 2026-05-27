package com.example.utils

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

object PdfHelper {

    fun generatePdf(
        context: Context,
        folderName: String,
        fileName: String,
        content: String
    ): File {
        // Clean folder name to create a safe localized directory under filesDir/Categories
        val safeFolderName = folderName.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim().replace(' ', '_')
        val folderDir = File(context.filesDir, "Categories/$safeFolderName")
        if (!folderDir.exists()) {
            folderDir.mkdirs()
        }

        // Clean document name and append pdf extension if missing
        var cleanFileName = fileName.trim().replace("/", "_")
        if (!cleanFileName.lowercase().endsWith(".pdf")) {
            cleanFileName += ".pdf"
        }

        val pdfFile = File(folderDir, cleanFileName)
        
        val pdfDocument = android.graphics.pdf.PdfDocument()
        
        // standard US Letter or A4 size (595 x 842 points for A4)
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paint background white
        canvas.drawColor(Color.WHITE)

        // Title styling
        val titlePaint = TextPaint().apply {
            color = Color.parseColor("#1A237E") // Deep Indigo header
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Metadata details
        val subtitlePaint = TextPaint().apply {
            color = Color.parseColor("#757575") // Charcoal grey
            textSize = 10f
            isAntiAlias = true
        }

        // Ruler line visual style
        val linePaint = android.graphics.Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 2f
            style = android.graphics.Paint.Style.STROKE
        }

        // Content styling (supports Hindi text correctly using system Devanagari typeface)
        val bodyPaint = TextPaint().apply {
            color = Color.parseColor("#212121") // Premium Off-black
            textSize = 14f
            isAntiAlias = true
        }

        // Draw actual text details
        val displayTitle = if (fileName.lowercase().endsWith(".pdf")) fileName.substring(0, fileName.length - 4) else fileName
        canvas.drawText(displayTitle, 40f, 60f, titlePaint)
        canvas.drawText("Folder: $folderName | View/Edit Language Compliant Document", 40f, 80f, subtitlePaint)
        
        // Divider
        canvas.drawLine(40f, 95f, 555f, 95f, linePaint)

        // Create elegant multi-line body flow
        val textWidth = 515 // 595 - 40 margin * 2
        val staticLayout = StaticLayout.Builder.obtain(
            content, 0, content.length, bodyPaint, textWidth
        )
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(4f, 1.0f)
        .build()

        canvas.save()
        canvas.translate(40f, 115f)
        staticLayout.draw(canvas)
        canvas.restore()

        pdfDocument.finishPage(page)
        
        FileOutputStream(pdfFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return pdfFile
    }
}
