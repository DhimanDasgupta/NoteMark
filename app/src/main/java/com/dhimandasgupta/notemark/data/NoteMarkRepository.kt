package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow

interface NoteMarkRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    suspend fun getRemoteNotes(page: Int = -1, size: Int = 20): Result<NoteResponse>
    suspend fun getRemoteNotesAndSaveInDB(page: Int = -1, size: Int = 20): Result<NoteResponse>
    suspend fun getAllNonSyncedNotes(): List<NoteEntity>
    suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity>
    suspend fun getNoteById(noteId: Long): NoteEntity?
    suspend fun getNoteByUUID(uuid: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateLocalNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity?

    suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean
    suspend fun updateRemoteNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Boolean

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

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> =
        localDataSource.getAllNonSyncedNotes()

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> =
        localDataSource.getAllMarkedAsDeletedNotes()

    override suspend fun getRemoteNotes(page: Int, size: Int): Result<NoteResponse> =
        remoteDataSource.getAllNotes(page = page, size = size)

    override suspend fun getRemoteNotesAndSaveInDB(page: Int, size: Int): Result<NoteResponse> {
        val remoteNotes = remoteDataSource.getAllNotes(page = page, size = size)
        remoteNotes.getOrNull()?.notes?.let { note ->
            val notesToBeSavedInDB = note.map { note -> note.toNoteEntity(synced = true) }
            return if (localDataSource.insertNotes(noteEntities = notesToBeSavedInDB)) {
                remoteNotes
            } else
                Result.failure(Exception("Failed to fetch notes from remote"))
        }
        return Result.failure(Exception("Failed to fetch notes from remote"))
    }

    override suspend fun getNoteById(noteId: Long) = localDataSource.getNoteById(noteId = noteId)

    override suspend fun getNoteByUUID(uuid: String) = localDataSource.getNoteByUUID(uuid = uuid)

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? =
        localDataSource.createNote(noteEntity = noteEntity.copy(synced = false))

    override suspend fun updateLocalNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? = localDataSource.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        uuid = noteEntity.uuid,
        synced = noteEntity.synced
    )

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) =
        localDataSource.insertNotes(noteEntities = noteEntities)

    override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean {
        val noteCreatedRemotely = remoteDataSource.createNote(noteEntity = noteEntity)
        return noteCreatedRemotely.getOrNull() != null
    }

    override suspend fun updateRemoteNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Boolean {
        val noteUpdatedRemotely = remoteDataSource.updateNote(
            title = title,
            content = content,
            lastEditedAt = lastEditedAt,
            noteEntity = noteEntity
        )
        return noteUpdatedRemotely.getOrNull() != null
    }

    override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
        val noteDeletedRemotely = remoteDataSource.deleteNote(noteEntity = noteEntity)
        return noteDeletedRemotely.getOrNull() == Unit
    }

    override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean {
        val noteDeletedLocally = localDataSource.deleteNote(noteEntity = noteEntity)
        return noteDeletedLocally
    }

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean =
        localDataSource.markAsDeleted(noteEntity = noteEntity)

    override suspend fun deleteAllLocalNotes() = try {
        localDataSource.deleteAllNotes()
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        false
    }

    override suspend fun logout(request: RefreshRequest): Result<Unit> =
        remoteDataSource.logout(request = request)
}
