package com.dhimandasgupta.notemark.data

import com.dhimandasgupta.notemark.data.remote.model.Note
import com.dhimandasgupta.notemark.database.NoteEntity

fun Note.toNoteEntity() = NoteEntity(
    id = 0L,
    title = title,
    content = content,
    createdAt = createdAt,
    lastEditedAt = lastEditedAt,
    uuid = uuid
)

fun NoteEntity.toNote() = Note(
    uuid = uuid,
    title = title,
    content = content,
    createdAt = createdAt,
    lastEditedAt = lastEditedAt
)