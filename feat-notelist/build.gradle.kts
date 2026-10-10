plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.notemark.android.compose)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.features.notelist"
}

dependencies {
  implementation(project(":common-android"))
  implementation(project(":common-compose"))
  implementation(project(":common-core"))
  implementation(project(":common-data"))
  implementation(project(":common-navigation-api"))
  implementation(project(":common-state-event-api"))
  implementation(project(":common-state-event-impl"))

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.material3)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  debugImplementation(libs.androidx.ui.tooling)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.material3.adaptive.navigation3)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.collections.immutable)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlin.protobuf)
  implementation(libs.flow.redux)
  implementation(libs.molecule)
  implementation(libs.androidx.paging.common)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(testFixtures(project(":common-state-event-impl")))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.kotlin)
}
