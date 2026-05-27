package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DocumentRepository
import com.example.data.Folder
import com.example.data.PdfDocumentEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DocumentRepository(application, database.documentDao())

    val folders: StateFlow<List<Folder>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolderId = MutableStateFlow<Int?>(null)
    val selectedFolderId: StateFlow<Int?> = _selectedFolderId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered documents combining folder filter and search queries
    val documents: StateFlow<List<PdfDocumentEntity>> = combine(
        repository.allDocuments,
        _selectedFolderId,
        _searchQuery
    ) { allDocs, folderId, query ->
        var filteredList = allDocs
        if (folderId != null) {
            filteredList = filteredList.filter { it.folderId == folderId }
        }
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.contentText.contains(query, ignoreCase = true)
            }
        }
        filteredList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFolder(folderId: Int?) {
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createFolder(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.createFolder(name, colorHex)
        }
    }

    fun saveDocument(
        id: Int = 0,
        folderId: Int,
        folderName: String,
        title: String,
        content: String,
        language: String,
        onSuccess: (PdfDocumentEntity) -> Unit
    ) {
        viewModelScope.launch {
            val savedId = repository.saveDocument(
                id = id,
                folderId = folderId,
                folderName = folderName,
                title = title,
                content = content,
                language = language
            )
            // Fetch the updated entity so navigation can receive correct reference
            val updatedDoc = repository.getDocumentById(savedId.toInt())
            if (updatedDoc != null) {
                onSuccess(updatedDoc)
            }
        }
    }

    fun deleteDocument(document: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            // Unselect folder filter if deleted
            if (_selectedFolderId.value == folder.id) {
                _selectedFolderId.value = null
            }
            repository.deleteFolder(folder)
        }
    }
}
