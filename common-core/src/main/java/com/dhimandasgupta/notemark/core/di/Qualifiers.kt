package com.dhimandasgupta.notemark.core.di

import dev.zacsweers.metro.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AppBackgroundDispatcher

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AppBackgroundScope

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class UserDataStore

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class SyncDataStore
