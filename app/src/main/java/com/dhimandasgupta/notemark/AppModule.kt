package com.dhimandasgupta.notemark

import UserManagerImpl
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhimandasgupta.notemark.data.NoteMarkRepository
import com.dhimandasgupta.notemark.data.NoteMarkRepositoryImpl
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSource
import com.dhimandasgupta.notemark.data.local.datasource.NoteMarkLocalDataSourceImpl
import com.dhimandasgupta.notemark.database.NoteMarkDatabase
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApi
import com.dhimandasgupta.notemark.data.remote.api.NoteMarkApiImpl
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSource
import com.dhimandasgupta.notemark.data.remote.datasource.NoteMarkApiDataSourceImpl
import com.dhimandasgupta.notemark.presenter.AppPresenter
import com.dhimandasgupta.notemark.presenter.LoginPresenter
import com.dhimandasgupta.notemark.presenter.RegistrationPresenter
import com.dhimandasgupta.notemark.statemachine.AppStateMachine
import com.dhimandasgupta.notemark.statemachine.LoginStateMachine
import com.dhimandasgupta.notemark.statemachine.RegistrationStateMachine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { AndroidSqliteDriver(NoteMarkDatabase.Schema, androidContext(), "app.db") } bind SqlDriver::class
    single { NoteMarkDatabase(get()) }
    singleOf(::UserManagerImpl) bind UserManager::class
    singleOf(::NoteMarkApiImpl) bind NoteMarkApi::class
    singleOf(::NoteMarkApiDataSourceImpl) bind NoteMarkApiDataSource::class
    singleOf(::NoteMarkLocalDataSourceImpl) bind NoteMarkLocalDataSource::class
    singleOf(::NoteMarkRepositoryImpl) bind NoteMarkRepository::class
    single { AppStateMachine(applicationContext = androidContext(), userManager = get(), noteMarkRepository = get()) }
    singleOf(::AppPresenter)

    factory { LoginStateMachine(noteMarkApi = get()) }
    factoryOf(::LoginPresenter)

    factory { RegistrationStateMachine(noteMarkApi = get()) }
    factoryOf(::RegistrationPresenter)
}