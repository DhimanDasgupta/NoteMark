package com.dhimandasgupta.notemark

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
    singleOf(::AppStateMachine)
    factoryOf(::AppPresenter)

    factoryOf(::LoginStateMachine)
    factoryOf(::LoginPresenter)

    factoryOf(::RegistrationStateMachine)
    factoryOf(::RegistrationPresenter)
}