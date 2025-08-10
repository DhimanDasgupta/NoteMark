package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.Long

class FakeSuccessfulNoteRepository : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = flowOf(
        value = listOf(noteEntity)
    )

    override suspend fun getRemoteNotesAndSaveInDB(
        page: Int,
        size: Int
    ): Result<NoteResponse> = Result.success(
        value = NoteResponse(
            notes = listOf(noteEntity.toNote()),
            total = 1
        )
    )

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> = listOf(
        noteEntity
    )

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> = listOf(
        noteEntity
    )

    override suspend fun getNoteById(noteId: Long): NoteEntity? = noteEntity

    override suspend fun getNoteByUUID(uuid: String): NoteEntity? = noteEntity

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? = noteEntity

    override suspend fun updateLocalNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? = noteEntity

    override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean = true

    override suspend fun updateRemoteNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Boolean = true

    override suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean = true

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean = true

    override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean = true

    override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean = true

    override suspend fun deleteAllLocalNotes(): Boolean = true

    override suspend fun logout(request: RefreshRequest): Result<Unit> =
        Result.success(Unit)
}

private val noteEntity = NoteEntity(
    id = 1,
    uuid = "some-uuid",
    title = "title",
    content = "content",
    createdAt = "2025-06-29T19:18:24.369Z",
    lastEditedAt = "2025-06-29T19:18:24.369Z",
    synced = true,
    markAsDeleted = false,
)