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
    suspend fun getAllNonSyncedNotes(): List<NoteEntity>
    suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity>
    suspend fun getNoteById(noteId: Long): NoteEntity?
    suspend fun getNoteByUUID(uuid: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity?
    suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        uuid: String
    ): NoteEntity?

    suspend fun insertNote(noteEntity: NoteEntity): Boolean
    suspend fun insertNotes(noteEntities: List<NoteEntity>): Boolean
    suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean
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
            .mapToList(context = Dispatchers.IO)
    }

    override suspend fun getAllNonSyncedNotes(): List<NoteEntity> =
        withContext(context = Dispatchers.IO) {
            return@withContext queries.getAllNonSyncedNotes().executeAsList()
        }

    override suspend fun getAllMarkedAsDeletedNotes(): List<NoteEntity> =
        withContext(context = Dispatchers.IO) {
            return@withContext queries.getAllDeletedNotes().executeAsList()
        }

    override suspend fun getNoteById(noteId: Long): NoteEntity? =
        withContext(context = Dispatchers.IO) {
            return@withContext queries.getNoteById(noteId).executeAsOneOrNull()
        }

    override suspend fun getNoteByUUID(uuid: String): NoteEntity? =
        withContext(context = Dispatchers.IO) {
            return@withContext queries.getNoteByUUID(uuid).executeAsOneOrNull()
        }

    override suspend fun updateNote(
        title: String,
        content: String,
        lastEditedAt: String,
        uuid: String
    ): NoteEntity = withContext(context = Dispatchers.IO) {
        val result = queries.updateNote(
            title,
            content,
            lastEditedAt,
            uuid
        )

        return@withContext (if (result.value == 1L) {
            queries.getNoteByUUID(uuid).executeAsOne()
        } else {
            null
        }) as NoteEntity
    }

    override suspend fun createNote(noteEntity: NoteEntity): NoteEntity =
        withContext(Dispatchers.IO) {
            val result = queries.insertNote(
                title = noteEntity.title,
                content = noteEntity.content,
                createdAt = noteEntity.createdAt,
                lastEditedAt = noteEntity.lastEditedAt,
                uuid = noteEntity.uuid,
                synced = false
            )

            return@withContext (if (result.value == 1L) {
                noteEntity
            } else {
                null
            }) as NoteEntity
        }

    override suspend fun insertNote(noteEntity: NoteEntity): Boolean =
        withContext(context = Dispatchers.IO) {
            val result = queries.transactionWithResult {
                queries.insertNote(
                    title = noteEntity.title,
                    content = noteEntity.content,
                    createdAt = noteEntity.createdAt,
                    lastEditedAt = noteEntity.lastEditedAt,
                    uuid = noteEntity.uuid,
                    synced = false
                )
                return@transactionWithResult true
            }
            return@withContext result
        }

    override suspend fun insertNotes(noteEntities: List<NoteEntity>) =
        withContext(context = Dispatchers.IO) {
            val result = queries.transactionWithResult {
                noteEntities.forEach { noteEntity ->
                    queries.insertNote(
                        title = noteEntity.title,
                        content = noteEntity.content,
                        createdAt = noteEntity.createdAt,
                        lastEditedAt = noteEntity.lastEditedAt,
                        uuid = noteEntity.uuid,
                        synced = false
                    )
                }
                return@transactionWithResult true
            }
            return@withContext result
        }

    override suspend fun markAsDeleted(noteEntity: NoteEntity): Boolean =
        withContext(context = Dispatchers.IO) {
            val result = queries.markNoteAsDeletedByUUID(uuid = noteEntity.uuid)
            return@withContext result.value == 1L
        }

    override suspend fun deleteNote(noteEntity: NoteEntity) =
        withContext(context = Dispatchers.IO) {
            val result = queries.deleteNoteByUUID(uuid = noteEntity.uuid)
            return@withContext result.value == 1L
        }

    override suspend fun deleteAllNotes() = withContext(context = Dispatchers.IO) {
        val result = queries.transactionWithResult {
            queries.deleteAll()
        }
        return@withContext result.value == 1L
    }
}