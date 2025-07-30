package com.dhimandasgupta.notemark.features.addnote

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.remote.model.NoteResponse
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.database.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddNoteStateMachineTest {
    @Test
    fun `test AddNoteStateMachine with default state`() = runTest {
        turbineScope {
            val stateMachine = AddNoteStateMachine(FakeSuccessfulNoteRepository())

            stateMachine.state.test {
                val initialState = awaitItem()
                assertEquals(AddNoteStateMachine.defaultAddNoteState, initialState)
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine with title and content`() = runTest {
        turbineScope {
            val stateMachine = AddNoteStateMachine(FakeSuccessfulNoteRepository())

            stateMachine.state.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine when create note is successful`() = runTest {
        turbineScope {
            val stateMachine = AddNoteStateMachine(FakeSuccessfulNoteRepository())

            stateMachine.state.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
                stateMachine.dispatch(AddNoteAction.Save)
                assertEquals(currentState.copy(title = "Title", content = "Content", saved = true), awaitItem())
            }
        }
    }

    @Test
    fun `test AddNoteStateMachine when create note is failed`() = runTest {
        turbineScope {
            val stateMachine = AddNoteStateMachine(FakeFailureNoteRepository())

            stateMachine.state.test {
                val currentState = awaitItem()
                stateMachine.dispatch(AddNoteAction.UpdateTitle("Title"))
                assertEquals(currentState.copy(title = "Title"), awaitItem())
                stateMachine.dispatch(AddNoteAction.UpdateContent("Content"))
                assertEquals(currentState.copy(title = "Title", content = "Content"), awaitItem())
                stateMachine.dispatch(AddNoteAction.Save)
                // Since on Failed Save, the state doesn't change hence no new items are emitted in the flow.
                expectNoEvents()
            }
        }
    }
}

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

class FakeSuccessfulNoteRepository : NoteMarkRepository {
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
        return noteEntity
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