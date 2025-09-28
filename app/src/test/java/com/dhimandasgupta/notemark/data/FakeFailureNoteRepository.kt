package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFailureNoteRepository : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = flowOf(
        value = emptyList()
    )

    override suspend fun getRemoteNotes(
        page: Int,
        size: Int
    ): Result<NoteResponse> = Result.failure(
        Exception("Some exception")
    )

    override suspend fun getRemoteNotesAndSaveInDB(
        page: Int,
        size: Int
    ): Result<NoteResponse> = Result.failure(
        Exception("Some exception")
    )

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> = emptyList()

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> = emptyList()

    override suspend fun getNoteById(noteId: Long): NoteEntity? = null

    override suspend fun getNoteByUUID(uuid: String): NoteEntity? = null

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? = null

    override suspend fun updateLocalNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? = null

    override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean = false

    override suspend fun updateRemoteNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Boolean = false

    override suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean = false

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean = false

    override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean = false

    override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean = false

    override suspend fun deleteAllLocalNotes(): Boolean = false

    override suspend fun logout(request: RefreshRequest): Result<Unit> = Result.failure(
        Exception("Some exception")
    )
}