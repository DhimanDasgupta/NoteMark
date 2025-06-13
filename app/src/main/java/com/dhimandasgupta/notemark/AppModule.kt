package com.dhimandasgupta.notemark

import com.dhimandasgupta.notemark.network.NoteMarkApi
import com.dhimandasgupta.notemark.network.storage.InMemoryTokenStorage
import com.dhimandasgupta.notemark.network.storage.TokenStorage
import com.dhimandasgupta.notemark.presenter.AppPresenter
import com.dhimandasgupta.notemark.presenter.LoginPresenter
import com.dhimandasgupta.notemark.presenter.RegistrationPresenter
import com.dhimandasgupta.notemark.statemachine.AppStateMachine
import com.dhimandasgupta.notemark.statemachine.LoginStateMachine
import com.dhimandasgupta.notemark.statemachine.RegistrationStateMachine
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    single<TokenStorage> { InMemoryTokenStorage() }
    singleOf(::NoteMarkApi)
    single { AppStateMachine(noteMarkApi = get(), tokenStorage = get()) }
    factoryOf(::AppPresenter)

    factory { LoginStateMachine(noteMarkApi = get()) }
    factoryOf(::LoginPresenter)

    factory { RegistrationStateMachine(noteMarkApi = get()) }
    factoryOf(::RegistrationPresenter)
}