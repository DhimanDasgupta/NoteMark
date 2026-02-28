package com.dhimandasgupta.notemark.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.app.work.NoteSyncWorker
import com.dhimandasgupta.notemark.common.storage.SyncSerializer
import com.dhimandasgupta.notemark.common.storage.UserSerializer
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.NoteMarkRepositoryImpl
import com.dhimandasgupta.notemark.data.SyncRepository
import com.dhimandasgupta.notemark.data.SyncRepositoryImpl
import com.dhimandasgupta.notemark.data.UserRepository
import com.dhimandasgupta.notemark.data.UserRepositoryImpl
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSourceImpl
import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSourceImpl
import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.data.local.datasource.UserDataSourceImpl
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApiImpl
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSourceImpl
import com.dhimandasgupta.notemark.data.remote.model.RefreshRequest
import com.dhimandasgupta.notemark.data.remote.model.RefreshResponse
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.addnote.AddNoteStateMachineFactory
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenter
import com.dhimandasgupta.notemark.features.editnote.EditNoteStateMachineFactory
import com.dhimandasgupta.notemark.features.launcher.AppStateMachineFactory
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.login.LoginStateMachineFactory
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListStateMachineFactory
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationStateMachineFactory
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import timber.log.Timber

// Define unique names for your DataStores
private enum class DataStoreType {
    USER_PREFERENCES,
    SYNC_PREFERENCES
}

const val APP_BACKGROUND_SCOPE = "app_background_scope"
private const val USER_DATA_STORE_FILE_NAME = "user_store.pb"
private const val SYNC_DATA_STORE_FILE_NAME = "sync_store.pb"

val appModule = module {
    single<CoroutineScope>(
        qualifier = named(name = APP_BACKGROUND_SCOPE)
    ) {
        CoroutineScope(
            context = Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
                Timber.e(message = "CoroutineExceptionHandler got $throwable in ${context.job} and ${Thread.currentThread()}")
            }
        )
    }
    single<DataStore<User>>(qualifier = named(DataStoreType.USER_PREFERENCES)) {
        DataStoreFactory.create(
            serializer = UserSerializer(),
            produceFile = { androidApplication().dataStoreFile(fileName = USER_DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            migrations = listOf(),
            scope = get(qualifier = named(name = APP_BACKGROUND_SCOPE))
        )
    }
    single {
        UserDataSourceImpl(
            userDataStore = get(qualifier = named(enum = DataStoreType.USER_PREFERENCES))
        )
    } bind UserDataSource::class
    single { UserRepositoryImpl(userDataSource = get()) } bind UserRepository::class
    single<DataStore<Sync>>(qualifier = named(enum = DataStoreType.SYNC_PREFERENCES)) {
        DataStoreFactory.create(
            serializer = SyncSerializer(),
            produceFile = { androidApplication().dataStoreFile(fileName = SYNC_DATA_STORE_FILE_NAME) },
            corruptionHandler = null,
            migrations = listOf(),
            scope = get(qualifier = named(name = APP_BACKGROUND_SCOPE))
        )
    }
    single {
        NoteSyncDataSourceImpl(
            syncDataStore = get(qualifier = named(enum = DataStoreType.SYNC_PREFERENCES))
        )
    } bind NoteSyncDataSource::class
    single { SyncRepositoryImpl(noteSyncDataSource = get()) } bind SyncRepository::class
    single {
        HttpClient(engineFactory = Android) {
            install(plugin = ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(plugin = Logging) {
                logger = Logger.ANDROID
                level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
            }

            install(plugin = Auth) {
                bearer {
                    loadTokens {
                        withContext(Dispatchers.IO) {
                            val user = get<UserRepository>().getUser().first()
                            if (user?.accessToken != null && user.refreshToken != null) {
                                BearerTokens(
                                    accessToken = user.accessToken,
                                    refreshToken = user.refreshToken
                                )
                            }
                            null
                        }
                    }
                    refreshTokens {
                        withContext(Dispatchers.IO) {
                            val user = get<UserRepository>().getUser().first()
                            val currentTokens =
                                if (user?.accessToken != null && user.refreshToken != null) {
                                    BearerTokens(
                                        accessToken = user.accessToken,
                                        refreshToken = user.refreshToken
                                    )
                                } else {
                                    return@withContext null
                                }

                            try {
                                val response = client.post {
                                    url(urlString = "/api/auth/refresh")
                                    markAsRefreshTokenRequest()
                                    contentType(type = Application.Json)
                                    setBody(
                                        RefreshRequest(
                                            refreshToken = currentTokens.refreshToken ?: ""
                                        )
                                    )
                                }.body<RefreshResponse>()

                                val newTokens = BearerTokens(
                                    accessToken = response.accessToken,
                                    refreshToken = response.refreshToken
                                )
                                get<UserRepository>().saveBearToken(token = newTokens)
                                newTokens
                            } catch (_: Exception) {
                                get<UserRepository>().deleteUser()
                                null
                            }
                        }
                    }
                }
            }
            defaultRequest {
                url(urlString = "https://notemark.pl-coding.com")
                header("X-User-Email", BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API)
                header("Debug", if (BuildConfig.DEBUG) "true" else "false") // Only here
            }
        }
    }
    single {
        AndroidSqliteDriver(
            schema = NoteMarkDatabase.Schema.synchronous(),
            context = androidContext(),
            name = "app.db",
            callback = object : AndroidSqliteDriver.Callback(NoteMarkDatabase.Schema.synchronous()) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Timber.d("Database created: onCreate, $db")
                }

                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    Timber.d("Database configured: onConfigure, $db")
                }

                override fun onCorruption(db: SupportSQLiteDatabase) {
                    super.onCorruption(db)
                    Timber.d("Database corrupted: onCorruption, $db")
                }

                override fun onDowngrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    super.onDowngrade(db, oldVersion, newVersion)
                    Timber.d("Database downgraded: onDowngrade, $db, oldVersion: $oldVersion, newVersion: $newVersion")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Timber.d("Database opened: onOpen, $db")
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    super.onUpgrade(db, oldVersion, newVersion)
                    Timber.d("Database upgraded: onUpgrade, $db, oldVersion: $oldVersion, newVersion: $newVersion")
                }
            }
        )
    } bind SqlDriver::class
    single { NoteMarkDatabase(driver = get()) }
    singleOf(constructor = ::NoteMarkApiImpl) bind NoteMarkApi::class
    singleOf(constructor = ::NoteMarkApiDataSourceImpl) bind NoteMarkApiDataSource::class
    singleOf(constructor = ::NoteMarkLocalDataSourceImpl) bind NoteMarkLocalDataSource::class
    single {
        NoteMarkRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get()
        )
    } bind NoteMarkRepository::class
    single {
        AppStateMachineFactory(
            applicationContext = androidContext(),
            userRepository = get(),
            syncRepository = get(),
            noteMarkRepository = get()
        )
    }
    factoryOf(constructor = ::LauncherPresenter)

    factory { LoginStateMachineFactory(noteMarkApi = get()) }
    factoryOf(constructor = ::LoginPresenter)

    factory { RegistrationStateMachineFactory(noteMarkApi = get()) }
    factoryOf(constructor = ::RegistrationPresenter)

    factory { NoteListStateMachineFactory(userRepository = get(), noteMarkRepository = get()) }
    factoryOf(constructor = ::NoteListPresenter)

    factory { AddNoteStateMachineFactory(noteMarkRepository = get()) }
    factoryOf(constructor = ::AddNotePresenter)

    factory { params ->
        require(params.isNotEmpty()) { "NoteId is required for ${EditNoteStateMachineFactory::class.java} to instantiate." }
        EditNoteStateMachineFactory(
            noteMarkRepository = get(),
            noteId = params[0]
        )
    }
    factory { params ->
        require(params.isNotEmpty()) { "NoteId is required for ${EditNotePresenter::class.java} to instantiate." }
        EditNotePresenter(noteId = params[0])
    }

    factoryOf(constructor = ::SettingsPresenter)

    worker { NoteSyncWorker(context = androidContext(), workerParameters = get()) }
}