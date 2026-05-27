package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val folderId: Int,
    val title: String,
    val contentText: String,
    val language: String = "Bilingual", // "Hindi", "English", or "Bilingual"
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
