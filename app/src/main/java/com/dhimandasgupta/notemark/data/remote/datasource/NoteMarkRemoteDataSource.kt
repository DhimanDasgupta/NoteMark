package com.dhimandasgupta.notemark.data.remote.datasource

import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.database.NoteEntity
import io.ktor.http.HttpStatusCode

interface NoteMarkApiDataSource {
    suspend fun getAllNotes(): List<NoteEntity>
    suspend fun getNoteById(noteId: String): NoteEntity?
    suspend fun createNote(noteEntity: NoteEntity): NoteEntity
    suspend fun updateNote(title: String, content: String, lastEditedAt: String, noteEntity: NoteEntity): NoteEntity
    suspend fun deleteNote(noteEntity: NoteEntity): Boolean
}

class NoteMarkApiDataSourceImpl(
    private val noteMarkApi: NoteMarkApi
) : NoteMarkApiDataSource {
    override suspend fun getAllNotes() = noteMarkApi.getNotes()

    override suspend fun getNoteById(noteId: String) = noteMarkApi.getNotes().find { it.id == noteId }

    override suspend fun createNote(noteEntity: NoteEntity) = noteMarkApi.createNote(noteEntity)

    override suspend fun updateNote(title: String, content: String, lastEditedAt: String, noteEntity: NoteEntity) = noteMarkApi.updateNote(
        title = title,
        content = content,
        lastEditedAt = lastEditedAt,
        noteEntity = noteEntity
    )

    override suspend fun deleteNote(noteEntity: NoteEntity): Boolean {
        val status = noteMarkApi.deleteNote(noteEntity)
        return status == HttpStatusCode.OK
    }
}