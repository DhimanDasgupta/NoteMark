package com.dhimandasgupta.notemark.app.di

import dev.zacsweers.metro.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AppBackgroundDispatcher

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AppBackgroundScope

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class UserDataStore

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class SyncDataStore
