package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

interface NoteMarkRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getRemoteNotesAndSaveInDB(page: Int = -1, size: Int = 20): Result<NoteResponse>
    suspend fun getAllNonSyncedNotes(): List<NoteEntity>
    suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity>
    suspend fun getNoteById(noteId: Long): NoteEntity?
    suspend fun getNoteByUUID(uuid: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateNote(title: String, content: String, lastEditedAt: String, noteEntity: NoteEntity): NoteEntity?
    suspend fun uploadNote(noteEntities: NoteEntity): Boolean
    suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean
    suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean
    suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean
    suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean
    suspend fun deleteAllLocalNotes(): Boolean
    suspend fun logout(request: RefreshRequest): Result<Unit>
}

class NoteMarkRepositoryImpl(
    private val localDataSource: NoteMarkLocalDataSource,
    private val remoteDataSource: NoteMarkApiDataSource
) : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = localDataSource.getAllNotes()

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> = localDataSource.getAllNonSyncedNotes()

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> = localDataSource.getAllMarkedAsDeletedNotes()

    override suspend fun getRemoteNotesAndSaveInDB(page: Int, size: Int): Result<NoteResponse> {
        val remoteNotes = remoteDataSource.getAllNotes(page, size)
        remoteNotes.getOrNull()?.notes?.let { note ->
            val notesToBeSavedInDB = note.map { note -> note.toNoteEntity(synced = true) }
            return if (localDataSource.insertNotes(notesToBeSavedInDB)) {
                remoteNotes
            } else
                Result.failure(Exception("Failed to fetch notes from remote"))
        }
        return Result.failure(Exception("Failed to fetch notes from remote"))
    }

    override suspend fun getNoteById(noteId: Long) = localDataSource.getNoteById(noteId)

    override suspend fun getNoteByUUID(uuid: String) = localDataSource.getNoteByUUID(uuid)

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? = localDataSource.createNote(noteEntity.copy(synced = false))

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? = localDataSource.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        uuid = noteEntity.uuid
    )

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) = localDataSource.insertNotes(noteEntities)

    override suspend fun uploadNote(noteEntities: NoteEntity): Boolean {
        val noteCreatedRemotely = remoteDataSource.createNote(noteEntities)
        return noteCreatedRemotely.getOrNull() != null
    }

    override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
        val noteDeletedRemotely = remoteDataSource.deleteNote(noteEntity)
        return noteDeletedRemotely.getOrNull() == Unit
    }

    override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean {
        val noteDeletedLocally = localDataSource.deleteNote(noteEntity)
        return noteDeletedLocally
    }

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean  = localDataSource.markAsDeleted(noteEntity)

    override suspend fun deleteAllLocalNotes() = try {
        localDataSource.deleteAllNotes()
    } catch (_: Exception) {
        coroutineContext.ensureActive()
        false
    }

    override suspend fun logout(request: RefreshRequest): Result<Unit> {
        val result = remoteDataSource.logout(request)
        result.onSuccess {
            withContext(NonCancellable) {
                localDataSource.deleteAllNotes()
            }
        }
        return result
    }
}
