package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.coroutineContext

interface NoteMarkRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getNoteById(noteId: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateNote(title: String, content: String, noteEntity: NoteEntity): NoteEntity?
    suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean
    suspend fun deleteNote(noteEntity: NoteEntity): Boolean
    suspend fun deleteAllNotes(): Boolean
}

class NoteMarkRepositoryImpl(
    private val localDataSource: NoteMarkLocalDataSource,
    private val remoteDataSource: NoteMarkApiDataSource
) : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = localDataSource.getAllNotes()

    override suspend fun getNoteById(noteId: String) = localDataSource.getNoteById(noteId)

    override suspend fun createNote(noteEntity: NoteEntity) = try {
        val note = remoteDataSource.createNote(noteEntity)
        localDataSource.createNote(note)
        note
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        null
    }

    override suspend fun updateNote(
        title: String,
        content: String,
        noteEntity: NoteEntity
    ) = try {
        val note = remoteDataSource.updateNote(title, content, noteEntity)
        localDataSource.updateNote(title, content, note)
        note
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        null
    }

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) = localDataSource.insertNotes(noteEntities)

    override suspend fun deleteNote(noteEntity: NoteEntity): Boolean {
        return try {
            val status = remoteDataSource.deleteNote(noteEntity)
            if (status) {
                return localDataSource.deleteNote(noteEntity)
            } else
                false
        } catch (_: Exception) {
            coroutineContext.ensureActive()
            false
        }
    }

    override suspend fun deleteAllNotes() = try {
        localDataSource.deleteAllNotes()
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        false
    }
}