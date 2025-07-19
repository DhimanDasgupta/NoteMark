package com.dhimandasgupta.notemark.app

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.common.storage.UserManager
import com.dhimandasgupta.notemark.common.storage.UserManagerImpl
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.NoteMarkRepositoryImpl
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSourceImpl
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApiImpl
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSourceImpl
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RefreshResponse
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenter
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.features.addnote.AddNoteStateMachine
import com.dhimandasgupta.notemark.features.editnote.EditNoteStateMachine
import com.dhimandasgupta.notemark.features.launcher.AppStateMachine
import com.dhimandasgupta.notemark.features.login.LoginStateMachine
import com.dhimandasgupta.notemark.features.notelist.NoteListStateMachine
import com.dhimandasgupta.notemark.features.registration.RegistrationStateMachine
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    singleOf(::UserManagerImpl) bind UserManager::class
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.ALL
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        get<UserManager>().getUser().first()?.bearerTokens
                    }
                    refreshTokens {
                        val currentTokens = get<UserManager>().getUser().firstOrNull()?.bearerTokens
                            ?: return@refreshTokens null

                        try {
                            val response = client.post {
                                url("/api/auth/refresh")
                                markAsRefreshTokenRequest()
                                contentType(Application.Json)
                                setBody(RefreshRequest(refreshToken = currentTokens.refreshToken ?: ""))
                            }.body<RefreshResponse>()

                            val newTokens = BearerTokens(response.accessToken, response.refreshToken)
                            get<UserManager>().saveToken(newTokens)
                            newTokens
                        } catch (_: Exception) {
                            get<UserManager>().clearUser()
                            null
                        }
                    }
                }
            }
            defaultRequest {
                url("https://notemark.pl-coding.com")
                header("X-User-Email", BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API)
                header("Debug", "true") // Only here
            }
        }
    }
    single { AndroidSqliteDriver(NoteMarkDatabase.Schema, androidContext(), "app.db") } bind SqlDriver::class
    single { NoteMarkDatabase(get()) }
    singleOf(::NoteMarkApiImpl) bind NoteMarkApi::class
    singleOf(::NoteMarkApiDataSourceImpl) bind NoteMarkApiDataSource::class
    singleOf(::NoteMarkLocalDataSourceImpl) bind NoteMarkLocalDataSource::class
    singleOf(::NoteMarkRepositoryImpl) bind NoteMarkRepository::class
    single {
        AppStateMachine(
            applicationContext = androidContext(),
            userManager = get(),
            noteMarkRepository = get()
        )
    }
    factoryOf(::LauncherPresenter)

    factory { LoginStateMachine(noteMarkApi = get()) }
    factoryOf(::LoginPresenter)

    factory { RegistrationStateMachine(noteMarkApi = get()) }
    factoryOf(::RegistrationPresenter)

    factory { NoteListStateMachine(userManager = get(), noteMarkRepository = get()) }
    factoryOf(::NoteListPresenter)

    factory { AddNoteStateMachine(noteMarkRepository = get()) }
    factoryOf(::AddNotePresenter)

    factory { EditNoteStateMachine(noteMarkRepository = get()) }
    factoryOf(::EditNotePresenter)

    factoryOf(::SettingsPresenter)
}