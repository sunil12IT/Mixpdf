package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Folder::class, PdfDocumentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_editor_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.documentDao())
                }
            }
        }

        private suspend fun populateDatabase(documentDao: DocumentDao) {
            // Add some initial categories for folder management
            documentDao.insertFolder(Folder(name = "Personal Docs", colorHex = "#FF5722"))
            documentDao.insertFolder(Folder(name = "Work & Business", colorHex = "#2196F3"))
            documentDao.insertFolder(Folder(name = "Hindi studies (अध्ययन)", colorHex = "#4CAF50"))
            documentDao.insertFolder(Folder(name = "English Drafting", colorHex = "#9C27B0"))
            documentDao.insertFolder(Folder(name = "General Archive", colorHex = "#607D8B"))
        }
    }
}
