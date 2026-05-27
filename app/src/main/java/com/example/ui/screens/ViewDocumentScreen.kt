package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.DocumentViewModel
import com.example.ui.components.PdfPageViewer
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDocumentScreen(
    docId: Int,
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit, // passes docId
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()

    val doc = remember(docId, documents) {
        documents.find { it.id == docId }
    }

    if (doc == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Document not found.")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val folderName = remember(doc.folderId, folders) {
        folders.find { it.id == doc.folderId }?.name ?: "Private"
    }

    val pdfFile = remember(doc.filePath) {
        File(doc.filePath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local PDF Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onNavigateToEdit(doc.id)
                    }, modifier = Modifier.testTag("edit_pdf")) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Text Content")
                    }
                    IconButton(onClick = {
                        viewModel.deleteDocument(doc)
                        onNavigateBack()
                        Toast.makeText(context, "Document Deleted", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Document")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // General Title Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(folderName, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text(doc.language, fontWeight = FontWeight.Bold) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Organizer Location Details
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Physical Storage Location",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = doc.filePath,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("PDF Path", doc.filePath)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Path copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Path",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PDF RENDER CARD
            Text(
                text = "Live PDF Sheet Renders (A4 Layout)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PdfPageViewer(
                pdfFile = pdfFile,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Raw Plain Text Extract Section
            Text(
                text = "Raw Text Content (संपादन हेतु मूल पाठ)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = doc.contentText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
