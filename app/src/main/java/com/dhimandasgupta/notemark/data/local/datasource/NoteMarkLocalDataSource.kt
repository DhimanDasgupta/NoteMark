package com.dhimandasgupta.notemark.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.dhimandasgupta.notemark.database.NoteEntity
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface NoteMarkLocalDataSource {
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getNoteById(noteId: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateNote(title: String, content: String, noteEntity: NoteEntity): NoteEntity?
    suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean
    suspend fun deleteNote(noteEntity: NoteEntity): Boolean
    suspend fun deleteAllNotes(): Boolean
}

class NoteMarkLocalDataSourceImpl(
    database: NoteMarkDatabase
) : NoteMarkLocalDataSource {
    private val queries = database.noteMarkDatabaseQueries

    override fun getAllNotes(): Flow<List<NoteEntity>> {
        return queries.getAllNotes()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun getNoteById(noteId: String): NoteEntity? = withContext(Dispatchers.IO) {
        return@withContext queries.getNoteById(noteId).executeAsOneOrNull()
    }

    override suspend fun updateNote(title: String, content: String, noteEntity: NoteEntity): NoteEntity = withContext(Dispatchers.IO) {
        val result = queries.updateNote(
            noteEntity.id,
            title,
            content,
            "" // TODO: Set lastEditedAt
        )

        return@withContext (if (result.value == 1L) {
            noteEntity
        } else {
            null
        }) as NoteEntity
    }

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity = withContext(Dispatchers.IO) {
        val result = queries.insertNote(
            noteEntity.id,
            noteEntity.title,
            noteEntity.content,
            noteEntity.createdAt,
            noteEntity.lastEditedAt
        )

        return@withContext (if (result.value == 1L) {
            noteEntity
        } else {
            null
        }) as NoteEntity
    }

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) = withContext(Dispatchers.IO) {
        val result = queries.transactionWithResult {
            noteEntities.forEach { noteEntity ->
                if (noteEntity.id.isEmpty()) {
                    rollback(false)
                }
                queries.insertNote(
                    noteEntity.id,
                    noteEntity.title,
                    noteEntity.content,
                    noteEntity.createdAt,
                    noteEntity.lastEditedAt
                )
            }
            return@transactionWithResult true
        }
        return@withContext result
    }

    override suspend fun deleteNote(noteEntity: NoteEntity) = withContext(Dispatchers.IO) {
        val result = queries.deleteNoteById(noteEntity.id)
        return@withContext result.value == 1L
    }

    override suspend fun deleteAllNotes() = withContext(Dispatchers.IO) {
        val result = queries.transactionWithResult {
            queries.deleteAll()
        }
        return@withContext result.value == 1L
    }
}