package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.coroutineContext

interface NoteMarkRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getRemoteNotes(): List<NoteEntity>
    suspend fun getNoteById(noteId: Long): NoteEntity?
    suspend fun getNoteByUUID(uuid: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateNote(title: String, content: String, lastEditedAt: String, noteEntity: NoteEntity): NoteEntity?
    suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean
    suspend fun deleteNote(noteEntity: NoteEntity): Boolean
    suspend fun deleteAllLocalNotes(): Boolean
}

class NoteMarkRepositoryImpl(
    private val localDataSource: NoteMarkLocalDataSource,
    private val remoteDataSource: NoteMarkApiDataSource
) : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = localDataSource.getAllNotes()

    override suspend fun getRemoteNotes(): List<NoteEntity> {
        val remoteNotes = remoteDataSource.getAllNotes()
        remoteNotes.getOrNull()?.notes?.let { notes ->
            val notesToBeSavedInDB = notes.map { note -> note.toNoteEntity() }
            return if (localDataSource.insertNotes(notesToBeSavedInDB)) {
                notesToBeSavedInDB
            } else
                emptyList()
        }
        return emptyList()
    }

    override suspend fun getNoteById(noteId: Long) = localDataSource.getNoteById(noteId)

    override suspend fun getNoteByUUID(uuid: String) = localDataSource.getNoteByUUID(uuid)

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? {
        val remoteNote = remoteDataSource.createNote(noteEntity)
        remoteNote.getOrNull()?.let { note ->
            val notesToBeSavedInDB = note.toNoteEntity()
            return if (localDataSource.createNote(notesToBeSavedInDB) != null) {
                notesToBeSavedInDB
            } else
                null
        }
        return null
    }

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? {
        val remoteNote = remoteDataSource.updateNote(
            title = title,
            content = content,
            lastEditedAt = lastEditedAt,
            noteEntity = noteEntity
        )

        remoteNote.getOrNull()?.let { note ->
            val notesToBeSavedInDB = note.toNoteEntity()
            return if (localDataSource.updateNote(
                    title = notesToBeSavedInDB.title,
                    content = notesToBeSavedInDB.content,
                    lastEditedAt = notesToBeSavedInDB.lastEditedAt,
                    uuid = notesToBeSavedInDB.uuid
            ) != null) {
                notesToBeSavedInDB
            } else
                null
        }
        return null
    }

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) = localDataSource.insertNotes(noteEntities)

    override suspend fun deleteNote(noteEntity: NoteEntity): Boolean {
        val noteDeletedRemotely = remoteDataSource.deleteNote(noteEntity)
        if (noteDeletedRemotely.getOrNull() == Unit) {
            if (localDataSource.deleteNote(noteEntity)) {
                return true
            } else
                false
        }
        return false
    }

    override suspend fun deleteAllLocalNotes() = try {
        localDataSource.deleteAllNotes()
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        false
    }
}
