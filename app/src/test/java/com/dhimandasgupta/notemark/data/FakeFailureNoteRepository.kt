package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow

class FakeFailureNoteRepository : NoteMarkRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun getRemoteNotesAndSaveInDB(
        page: Int,
        size: Int
    ): Result<NoteResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getNoteById(noteId: Long): NoteEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getNoteByUUID(uuid: String): NoteEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity? {
        return null
    }

    override suspend fun updateLocalNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): NoteEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun createNewRemoteNote(noteEntity: NoteEntity): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteNote(
        title: String,
        content: String,
        lastEditedAt: String,
        noteEntity: NoteEntity
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRemoteNote(noteEntity: NoteEntity): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocalNote(noteEntity: NoteEntity): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllLocalNotes(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun logout(request: RefreshRequest): Result<Unit> {
        TODO("Not yet implemented")
    }

}