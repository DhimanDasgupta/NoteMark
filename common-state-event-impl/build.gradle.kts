plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.common.state.impl"

  // Fakes shared with tests of modules built on the app state machine.
  testFixtures { enable = true }
}

dependencies {
  implementation(project(":common-android"))
  implementation(project(":common-core"))
  implementation(project(":common-data"))
  implementation(project(":common-state-event-api"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlin.protobuf)
  implementation(libs.flow.redux)

  testFixturesImplementation(project(":common-android"))
  testFixturesImplementation(project(":common-data"))
  testFixturesImplementation(platform(libs.androidx.compose.bom))
  testFixturesImplementation(libs.androidx.ui)
  testFixturesImplementation(libs.kotlinx.coroutines.core)
  testFixturesImplementation(libs.kotlin.protobuf)
  testFixturesImplementation(platform(libs.ktor.bom))
  testFixturesImplementation(libs.ktor.client.auth)
  testFixturesImplementation(libs.mockito.core)
  testFixturesImplementation(libs.mockito.kotlin)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
