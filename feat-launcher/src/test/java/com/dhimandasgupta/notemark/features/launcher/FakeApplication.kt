package com.dhimandasgupta.notemark.features.launcher

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/** An Application whose package manager reports [versionName], as AppStateMachineFactory needs. */
fun fakeApplication(versionName: String): Application {
  val packageManager = mock<PackageManager>()
  whenever(packageManager.getPackageInfo(any<String>(), any<Int>()))
    .doReturn(PackageInfo().apply { this.versionName = versionName })

  val application = mock<Application>()
  whenever(application.applicationContext).doReturn(application)
  whenever(application.packageManager).doReturn(packageManager)
  whenever(application.packageName).doReturn("com.dhimandasgupta.notemark")
  return application
}
