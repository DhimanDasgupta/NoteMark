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
  api(project(":common-state-event-api"))
  // AppStateMachineFactory extends FlowReduxStateMachineFactory.
  api(libs.flow.redux)

  implementation(project(":common-android"))
  implementation(project(":common-data"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)

  testFixturesImplementation(project(":common-android"))
  testFixturesImplementation(project(":common-data"))
  testFixturesImplementation(libs.mockito.core)
  testFixturesImplementation(libs.mockito.kotlin)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
