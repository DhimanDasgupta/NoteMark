plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.state.impl"
}

dependencies {
  implementation(project(":common-android"))
  implementation(project(":common-core"))
  implementation(project(":common-data"))
  implementation(project(":common-state-event-api"))

  implementation(libs.androidx.ui)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlin.protobuf)
  implementation(libs.flow.redux)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(platform(libs.ktor.bom))
  testImplementation(libs.ktor.client.auth)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
