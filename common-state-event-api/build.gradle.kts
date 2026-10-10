plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.state"
}

dependencies {
  implementation(project(":common-android"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
}
