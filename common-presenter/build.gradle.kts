plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.presenter"
}

dependencies {
  implementation(project(":common-state-event-api"))
  implementation(project(":common-state-event-impl"))

  implementation(libs.androidx.ui)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.flow.redux)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(project(":common-android"))
  testImplementation(project(":common-data"))
  testImplementation(platform(libs.ktor.bom))
  testImplementation(libs.ktor.client.auth)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.kotlin.protobuf)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.molecule)
  testImplementation(libs.turbine)
}
