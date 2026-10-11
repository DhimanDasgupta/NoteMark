package com.dhimandasgupta.notemark.app.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhimandasgupta.notemark.BuildConfig
import com.dhimandasgupta.notemark.app.extension.ShortcutManagerAppShortcuts
import com.dhimandasgupta.notemark.app.extension.WorkManagerSyncScheduler
import com.dhimandasgupta.notemark.common.android.AppShortcuts
import com.dhimandasgupta.notemark.common.android.ConnectivityObserver
import com.dhimandasgupta.notemark.common.android.SyncScheduler
import com.dhimandasgupta.notemark.common.android.observeConnectivityAsFlow
import com.dhimandasgupta.notemark.core.di.AppBackgroundDispatcher
import com.dhimandasgupta.notemark.core.di.AppBackgroundScope
import com.dhimandasgupta.notemark.core.di.SyncDataStore
import com.dhimandasgupta.notemark.core.di.UserDataStore
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSourceImpl
import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteSyncDataSourceImpl
import com.dhimandasgupta.notemark.data.local.datasource.UserDataSource
import com.dhimandasgupta.notemark.data.local.datasource.UserDataSourceImpl
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApiImpl
import com.dhimandasgupta.notemark.data.remote.client.NoteMarkHttpClientFactory
import com.dhimandasgupta.notemark.data.remote.client.NoteMarkHttpConfig
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSourceImpl
import com.dhimandasgupta.notemark.data.repository.NoteMarkRepository
import com.dhimandasgupta.notemark.data.repository.NoteMarkRepositoryImpl
import com.dhimandasgupta.notemark.data.repository.SyncRepository
import com.dhimandasgupta.notemark.data.repository.SyncRepositoryImpl
import com.dhimandasgupta.notemark.data.repository.UserRepository
import com.dhimandasgupta.notemark.data.repository.UserRepositoryImpl
import com.dhimandasgupta.notemark.data.storage.SyncSerializer
import com.dhimandasgupta.notemark.data.storage.UserSerializer
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import com.dhimandasgupta.notemark.features.addnote.AddNotePresenter
import com.dhimandasgupta.notemark.features.editnote.EditNotePresenter
import com.dhimandasgupta.notemark.features.launcher.LauncherPresenter
import com.dhimandasgupta.notemark.features.login.LoginPresenter
import com.dhimandasgupta.notemark.features.notelist.NoteListPresenter
import com.dhimandasgupta.notemark.features.registration.RegistrationPresenter
import com.dhimandasgupta.notemark.features.settings.SettingsPresenter
import com.dhimandasgupta.notemark.proto.Sync
import com.dhimandasgupta.notemark.proto.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import kotlin.collections.listOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import timber.log.Timber

private const val USER_DATA_STORE_FILE_NAME = "user_store.pb"
private const val SYNC_DATA_STORE_FILE_NAME = "sync_store.pb"

val LocalNoteMarkGraph =
  staticCompositionLocalOf<NoteMarkGraph> {
    error("No NoteMarkGraph provided")
  }

@DependencyGraph(AppScope::class)
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

  fun workerFactory(): MetroWorkerFactory
}

@AssistedFactory
interface EditNotePresenterFactory {
  fun create(noteId: String): EditNotePresenter
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
  ): CoroutineScope =
    CoroutineScope(
      context =
        dispatcher +
          SupervisorJob() +
          CoroutineExceptionHandler { context, throwable ->
            Timber.e(
              "CoroutineExceptionHandler got $throwable in ${context.job} and ${Thread.currentThread()}"
            )
          }
    )

  @Provides
  @SingleIn(AppScope::class)
  @UserDataStore
  fun provideUserDataStore(
    context: Context,
    @AppBackgroundScope scope: CoroutineScope,
  ): DataStore<User> =
    DataStoreFactory.create(
      serializer = UserSerializer(),
      produceFile = { context.dataStoreFile(fileName = USER_DATA_STORE_FILE_NAME) },
      corruptionHandler = null,
      migrations = listOf(),
      scope = scope,
    )

  @Provides
  @SingleIn(AppScope::class)
  @SyncDataStore
  fun provideSyncDataStore(
    context: Context,
    @AppBackgroundScope scope: CoroutineScope,
  ): DataStore<Sync> =
    DataStoreFactory.create(
      serializer = SyncSerializer(),
      produceFile = { context.dataStoreFile(fileName = SYNC_DATA_STORE_FILE_NAME) },
      corruptionHandler = null,
      migrations = listOf(),
      scope = scope,
    )

  @Provides fun provideHttpClientEngine(): HttpClientEngine = Android.create()

  @Provides
  fun provideNoteMarkHttpConfig(): NoteMarkHttpConfig =
    NoteMarkHttpConfig(
      baseUrl = "https://notemark.pl-coding.com",
      userEmailHeader = BuildConfig.HEADER_VALUE_FOR_NOTE_MARK_API,
      isDebug = BuildConfig.DEBUG,
    )

  @Provides
  @SingleIn(AppScope::class)
  fun provideHttpClient(factory: NoteMarkHttpClientFactory): HttpClient = factory.create()

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

  @Provides
  @SingleIn(AppScope::class)
  fun provideSyncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideAppShortcuts(impl: ShortcutManagerAppShortcuts): AppShortcuts = impl

  @Provides
  @SingleIn(AppScope::class)
  fun provideConnectivityObserver(context: Context): ConnectivityObserver = ConnectivityObserver {
    context.observeConnectivityAsFlow()
  }
}
