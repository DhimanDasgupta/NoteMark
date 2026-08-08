package com.dhimandasgupta.notemark.app.di

import android.content.Context
import android.os.StrictMode
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhimandasgupta.notemark.BuildConfig
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
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenterFactory
import com.dhimandasgupta.notemark.features.editnote.EditNoteStateMachineFactoryFactory
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.collections.listOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

private const val USER_DATA_STORE_FILE_NAME = "user_store.pb"
private const val SYNC_DATA_STORE_FILE_NAME = "sync_store.pb"

@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface NoteMarkGraph : AppModule {
  @DependencyGraph.Factory
  fun interface Factory {
    fun create(@Provides context: Context): NoteMarkGraph
  }

  fun launcherPresenter(): LauncherPresenter

  fun loginPresenter(): LoginPresenter

  fun registrationPresenter(): RegistrationPresenter

  fun noteListPresenter(): NoteListPresenter

  fun addNotePresenter(): AddNotePresenter

  fun settingsPresenter(): SettingsPresenter

  fun editNotePresenterFactory(): EditNotePresenterFactory

  fun editNoteStateMachineFactoryFactory(): EditNoteStateMachineFactoryFactory

  fun workerFactory(): MetroWorkerFactory
}

val LocalNoteMarkGraph =
  staticCompositionLocalOf<NoteMarkGraph> {
    error("No NoteMarkGraph provided")
  }

interface AppModule {
  @Provides
  @SingleIn(AppScope::class)
  @AppBackgroundDispatcher
  fun provideAppBackgroundDispatcher(): CoroutineDispatcher =
    Dispatchers.IO.limitedParallelism(
      parallelism = 2,
      name = "AppBackgroundDispatcher",
    )

  @Provides
  @SingleIn(AppScope::class)
  @AppBackgroundScope
  fun provideAppBackgroundScope(
    @AppBackgroundDispatcher dispatcher: CoroutineDispatcher
  ): CoroutineScope {
    return CoroutineScope(
      context =
        dispatcher +
          SupervisorJob() +
          CoroutineExceptionHandler { context, throwable ->
            Timber.e(
              "CoroutineExceptionHandler got $throwable in ${context.job} and ${Thread.currentThread()}"
            )
          }
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @UserDataStore
  fun provideUserDataStore(
    context: Context,
    @AppBackgroundScope scope: CoroutineScope,
  ): DataStore<User> {
    return DataStoreFactory.create(
      serializer = UserSerializer(),
      produceFile = { context.dataStoreFile(fileName = USER_DATA_STORE_FILE_NAME) },
      corruptionHandler = null,
      migrations = listOf(),
      scope = scope,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  @SyncDataStore
  fun provideSyncDataStore(
    context: Context,
    @AppBackgroundScope scope: CoroutineScope,
  ): DataStore<Sync> {
    return DataStoreFactory.create(
      serializer = SyncSerializer(),
      produceFile = { context.dataStoreFile(fileName = SYNC_DATA_STORE_FILE_NAME) },
      corruptionHandler = null,
      migrations = listOf(),
      scope = scope,
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideHttpClient(
    @AppBackgroundDispatcher dispatcher: CoroutineDispatcher,
    userRepository: UserRepository,
  ): HttpClient {
    return StrictMode.allowThreadDiskReads().run {
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
              withContext(dispatcher) {
                val user = userRepository.getUser().first()
                if (user?.accessToken != null && user.refreshToken != null) {
                  BearerTokens(
                    accessToken = user.accessToken,
                    refreshToken = user.refreshToken,
                  )
                }
                null
              }
            }
            refreshTokens {
              withContext(dispatcher) {
                val user = userRepository.getUser().first()
                val currentTokens =
                  if (user?.accessToken != null && user.refreshToken != null) {
                    BearerTokens(
                      accessToken = user.accessToken,
                      refreshToken = user.refreshToken,
                    )
                  } else {
                    return@withContext null
                  }

                try {
                  val response =
                    client
                      .post {
                        url(urlString = "/api/auth/refresh")
                        markAsRefreshTokenRequest()
                        contentType(type = ContentType.Application.Json)
                        setBody(RefreshRequest(refreshToken = currentTokens.refreshToken ?: ""))
                      }
                      .body<RefreshResponse>()

                  val newTokens =
                    BearerTokens(
                      accessToken = response.accessToken,
                      refreshToken = response.refreshToken,
                    )
                  userRepository.saveBearToken(token = newTokens)
                  newTokens
                } catch (_: Exception) {
                  userRepository.deleteUser()
                  null
                }
              }
            }
          }
        }
        defaultRequest {
          url(urlString = "https://notemark.pl-coding.com")
          header("X-User-Email", BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API)
          header("Debug", if (BuildConfig.DEBUG) "true" else "false")
        }
      }
    }
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideSqlDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(
      schema = NoteMarkDatabase.Schema.synchronous(),
      context = context,
      name = "app.db",
      callback =
        object : AndroidSqliteDriver.Callback(NoteMarkDatabase.Schema.synchronous()) {
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

          override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            super.onDowngrade(db, oldVersion, newVersion)
            Timber.d(
              "Database downgraded: onDowngrade, $db, oldVersion: $oldVersion, newVersion: $newVersion"
            )
          }

          override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Timber.d("Database opened: onOpen, $db")
          }

          override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
            super.onUpgrade(db, oldVersion, newVersion)
            Timber.d(
              "Database upgraded: onUpgrade, $db, oldVersion: $oldVersion, newVersion: $newVersion"
            )
          }
        },
    )
  }

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteMarkDatabase(driver: SqlDriver): NoteMarkDatabase =
    NoteMarkDatabase(driver = driver)

  @Provides
  @SingleIn(AppScope::class)
  fun provideUserDataSource(impl: UserDataSourceImpl): UserDataSource = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteSyncDataSource(impl: NoteSyncDataSourceImpl): NoteSyncDataSource = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideSyncRepository(impl: SyncRepositoryImpl): SyncRepository = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteMarkApi(impl: NoteMarkApiImpl): NoteMarkApi = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteMarkApiDataSource(impl: NoteMarkApiDataSourceImpl): NoteMarkApiDataSource = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteMarkLocalDataSource(impl: NoteMarkLocalDataSourceImpl): NoteMarkLocalDataSource =
    impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideNoteMarkRepository(impl: NoteMarkRepositoryImpl): NoteMarkRepository = impl
}
