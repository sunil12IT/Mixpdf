package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM pdf_documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun getDocumentsInFolder(folderId: Int): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Int): PdfDocumentEntity?

    @Query("SELECT * FROM pdf_documents WHERE title LIKE '%' || :query || '%' OR contentText LIKE '%' || :query || '%'")
    fun searchDocuments(query: String): Flow<List<PdfDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: PdfDocumentEntity): Long

    @Delete
    suspend fun deleteDocument(document: PdfDocumentEntity)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("DELETE FROM pdf_documents WHERE folderId = :folderId")
    suspend fun deleteDocumentsByFolderId(folderId: Int)
}
