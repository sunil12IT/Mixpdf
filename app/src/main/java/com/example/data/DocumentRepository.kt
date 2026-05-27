package com.example.data

import android.content.Context
import com.example.utils.PdfHelper
import kotlinx.coroutines.flow.Flow
import java.io.File

class DocumentRepository(private val context: Context, private val documentDao: DocumentDao) {
    val allFolders: Flow<List<Folder>> = documentDao.getAllFolders()
    val allDocuments: Flow<List<PdfDocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentsInFolder(folderId: Int): Flow<List<PdfDocumentEntity>> {
        return documentDao.getDocumentsInFolder(folderId)
    }

    fun searchDocuments(query: String): Flow<List<PdfDocumentEntity>> {
        return documentDao.searchDocuments(query)
    }

    suspend fun getDocumentById(id: Int): PdfDocumentEntity? {
        return documentDao.getDocumentById(id)
    }

    suspend fun createFolder(name: String, colorHex: String): Long {
        return documentDao.insertFolder(Folder(name = name, colorHex = colorHex))
    }

    suspend fun saveDocument(
        id: Int = 0,
        folderId: Int,
        folderName: String,
        title: String,
        content: String,
        language: String
    ): Long {
        // Sync 1: Generate physical PDF in correct localized categorized path
        val pdfFile = PdfHelper.generatePdf(context, folderName, title, content)
        
        // Sync 2: Save metadata inside SQL
        val docEntity = PdfDocumentEntity(
            id = id,
            folderId = folderId,
            title = if (title.lowercase().endsWith(".pdf")) title else "$title.pdf",
            contentText = content,
            language = language,
            filePath = pdfFile.absolutePath,
            updatedAt = System.currentTimeMillis()
        )
        return documentDao.insertDocument(docEntity)
    }

    suspend fun deleteDocument(document: PdfDocumentEntity) {
        try {
            val file = File(document.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        documentDao.deleteDocument(document)
    }

    suspend fun deleteFolder(folder: Folder) {
        try {
            val safeFolderName = folder.name.replace(Regex("[^a-zA-Z0-9\\s]"), "").trim().replace(' ', '_')
            val folderDir = File(context.filesDir, "Categories/$safeFolderName")
            if (folderDir.exists()) {
                folderDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        documentDao.deleteDocumentsByFolderId(folder.id)
        documentDao.deleteFolder(folder)
    }
}
