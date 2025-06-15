package com.dhimandasgupta.notemark

import UserManagerImpl
import com.dhimandasgupta.notemark.network.api.NoteMarkApi
import com.dhimandasgupta.notemark.network.api.NoteMarkApiImpl
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
    singleOf(::UserManagerImpl) bind UserManager::class
    singleOf(::NoteMarkApiImpl) bind NoteMarkApi::class
    single { AppStateMachine(applicationContext = androidContext(), userManager = get()) }
    singleOf(::AppPresenter)

    factory { LoginStateMachine(noteMarkApi = get()) }
    factoryOf(::LoginPresenter)

    factory { RegistrationStateMachine(noteMarkApi = get()) }
    factoryOf(::RegistrationPresenter)
}