package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.DocumentViewModel
import com.example.data.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDocumentScreen(
    docId: Int, // if -1, we are creating a new doc
    folderId: Int, // default folderId
    viewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (Int) -> Unit, // passes saved docId
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()

    val existingDoc = remember(docId, documents) {
        if (docId != -1) documents.find { it.id == docId } else null
    }

    var title by remember { mutableStateOf("") }
    var contentState by remember { mutableStateOf(TextFieldValue("")) }
    var selectedLang by remember { mutableStateOf("Bilingual") }
    var documentFolderId by remember { mutableStateOf(folderId) }
    var isInitialized by remember { mutableStateOf(false) }

    // Once existing document loads, populate the states
    LaunchedEffect(existingDoc) {
        if (!isInitialized) {
            existingDoc?.let {
                title = it.title.removeSuffix(".pdf")
                contentState = TextFieldValue(it.contentText)
                selectedLang = it.language
                documentFolderId = it.folderId
            } ?: run {
                title = ""
                contentState = TextFieldValue("")
                selectedLang = "Bilingual"
                documentFolderId = folderId
            }
            isInitialized = true
        }
    }

    val selectedFolderObj = remember(documentFolderId, folders) {
        folders.find { it.id == documentFolderId } ?: folders.firstOrNull()
    }

    val hindiVowels = listOf(
        "ा" to "ा", "ि" to "ि", "ी" to "ी", "ु" to "ु", "ू" to "ू",
        "े" to "े", "ै" to "ै", "ो" to "ो", "ौ" to "ौ", "ं" to "ं", "्" to "्"
    )

    val hindiWords = listOf(
        "नमस्ते" to "नमस्ते", "धन्यवाद" to "धन्यवाद", "कृपया" to "कृपया",
        "है" to "है", "हैं" to "हैं", "और" to "और", "में" to "में",
        "का" to "का", "की" to "की", "को" to "को", "था" to "था"
    )

    // Cursor-aware insertion function
    fun insertTextAtCursor(textToInsert: String) {
        val currentText = contentState.text
        val selectionStart = contentState.selection.start
        val selectionEnd = contentState.selection.end

        val newText = currentText.substring(0, selectionStart) + textToInsert + currentText.substring(selectionEnd)
        val newCursorPosition = selectionStart + textToInsert.length

        contentState = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPosition)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (docId == -1) "Create Local PDF" else "Edit Text Content", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Please enter a document title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (contentState.text.isBlank()) {
                                Toast.makeText(context, "Content cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val folder = selectedFolderObj
                            if (folder != null) {
                                viewModel.saveDocument(
                                    id = if (docId == -1) 0 else docId,
                                    folderId = folder.id,
                                    folderName = folder.name,
                                    title = title.trim(),
                                    content = contentState.text,
                                    language = selectedLang,
                                    onSuccess = { savedDoc ->
                                        Toast.makeText(context, "PDF saved successfully local!", Toast.LENGTH_SHORT).show()
                                        onSaveSuccess(savedDoc.id)
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_pdf_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Document Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title (e.g. Hindi Translation Notes)") },
                    placeholder = { Text("E.g. Invoice, Draft, अध्ययन पत्र") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_title_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Dropdown or Selection Cards for Folder Category
                Column {
                    Text(
                        "Destination PDF Folder Category",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        folders.forEach { folder ->
                            val isSelected = documentFolderId == folder.id
                            val folderColor = Color(android.graphics.Color.parseColor(folder.colorHex))

                            Surface(
                                selected = isSelected,
                                onClick = { documentFolderId = folder.id },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) folderColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) BorderStroke(2.dp, folderColor) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Folder,
                                        contentDescription = null,
                                        tint = folderColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = folder.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) folderColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Language Segment Selection
                Column {
                    Text(
                        "Document Primary Script / Language",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Bilingual", "Hindi", "English").forEach { lang ->
                            val isSelected = selectedLang == lang
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = { selectedLang = lang },
                                label = { Text(lang, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Editor Body Text Area
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Document Content Draft (Hindi / English)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = contentState,
                        onValueChange = { contentState = it },
                        placeholder = { Text("हिंदी में टाइप करें या अंग्रेजी में लिखें... Use the toolbar below to insert characters easily.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                            .testTag("pdf_content_input"),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 100
                    )
                }
            }

            // STATIC HINDI TYPING ASSISTANT PANEL (Sticks to bottom, does not scroll with main document body)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "हिन्दी शब्द सहायक (Hindi Language Toolbar)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Words row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp)
                    ) {
                        hindiWords.forEach { (label, value) ->
                            AssistChip(
                                onClick = { insertTextAtCursor(value) },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    // Vowels row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        hindiVowels.forEach { (label, value) ->
                            Button(
                                onClick = { insertTextAtCursor(value) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .widthIn(min = 36.dp)
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
