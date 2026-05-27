package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.DocumentViewModel
import com.example.data.Folder
import com.example.data.PdfDocumentEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DocumentViewModel,
    onNavigateToView: (Int) -> Unit,
    onNavigateToCreate: (Int) -> Unit, // passes folder ID
    modifier: Modifier = Modifier
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#3F51B5") }

    val colorPalette = listOf(
        "#3F51B5", // Royal Blue
        "#E91E63", // Pink
        "#FF9800", // Gold/Orange
        "#4CAF50", // Green
        "#9C27B0", // Purple
        "#00BCD4", // Cyan
        "#607D8B"  // Slate grey
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "लेख",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "लेख PDF",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreateFolderDialog = true },
                        modifier = Modifier.testTag("add_folder")
                    ) {
                        Icon(
                            Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    // Create in currently active folder, or default to first folder
                    val targetFolderId = selectedFolderId ?: folders.firstOrNull()?.id ?: 1
                    onNavigateToCreate(targetFolderId)
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add PDF") },
                text = { Text("New PDF Document") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("create_document_fab")
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search Hindi or English PDFs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_field"),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Category Folders Section Header
            Text(
                text = "Categorized Folders",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
            )

            // Folders Horizontal List
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "ALL" Filter Card
                item {
                    FolderCard(
                        name = "All Files",
                        color = MaterialTheme.colorScheme.secondary,
                        count = documents.size,
                        isSelected = selectedFolderId == null,
                        onClick = { viewModel.selectFolder(null) },
                        onDelete = null
                    )
                }

                items(folders, key = { it.id }) { folder ->
                    val folderDocs = documents.filter { it.folderId == folder.id }
                    FolderCard(
                        name = folder.name,
                        color = Color(android.graphics.Color.parseColor(folder.colorHex)),
                        count = folderDocs.size,
                        isSelected = selectedFolderId == folder.id,
                        onClick = { viewModel.selectFolder(folder.id) },
                        onDelete = { viewModel.deleteFolder(folder) }
                    )
                }
            }

            // Document Lists Header (Filters visual information)
            val filteredTitle = if (selectedFolderId != null) {
                folders.find { it.id == selectedFolderId }?.name ?: "Selected Folder"
            } else {
                "Recent Local PDFs"
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$filteredTitle (${documents.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )

            // Document List
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PictureAsPdf,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No PDF files found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No files match your search filter." else "Create a new Hindi or English PDF locally using categorized folders.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItemRow(
                            doc = doc,
                            folderName = folders.find { it.id == doc.folderId }?.name ?: "Private",
                            onClick = { onNavigateToView(doc.id) },
                            onDelete = { viewModel.deleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    // Modal Create Folder Custom Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name (Hindi or English)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_folder_name_field")
                    )

                    Text("Label Accent Space", style = MaterialTheme.typography.bodySmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorPalette.forEach { hex ->
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { selectedColorHex = hex }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName.trim(), selectedColorHex)
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_folder")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FolderCard(
    name: String,
    color: Color,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(136.dp)
            .height(118.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.FolderOpen else Icons.Filled.Folder,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else color,
                    modifier = Modifier.size(28.dp)
                )

                if (onDelete != null) {
                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Folder",
                            tint = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count items",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun DocumentItemRow(
    doc: PdfDocumentEntity,
    folderName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(doc.updatedAt) {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(Date(doc.updatedAt))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF Visual Icon Badge matching Professional Polish Container: background-[#FFDADA], icon-[#BA1A1A]
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(folderName, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(20.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    Text(
                        text = doc.language,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Saved: $dateString",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action delete icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete PDF metadata",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
