plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.state"
}

dependencies {
  // ConnectionState is part of AppState's public API.
  api(project(":common-android"))
  api(project(":common-data"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
}
