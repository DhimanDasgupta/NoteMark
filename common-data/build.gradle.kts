plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.sqlDelight)
  alias(libs.plugins.google.protobuf)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.data"

  // NoteMarkApi fakes shared with the tests of feature modules.
  testFixtures { enable = true }
}

sqldelight {
  databases {
    create("NoteMarkDatabase") {
      packageName.set("com.dhimandasgupta.notemark.database")
      generateAsync.set(true)
    }
  }
}

protobuf {
  protoc {
    artifact = libs.protobuf.protoc.get().toString()
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        register("kotlin") {
          option("lite")
        }
        register("java") {
          option("lite")
        }
      }
    }
  }
}

dependencies {
  implementation(project(":common-core"))

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlin.protobuf)
  implementation(libs.sql.delight.runtime)
  implementation(libs.sql.delight.async.extensions)
  implementation(libs.sql.delight.coroutines.extensions)
  implementation(libs.sql.delight.paging3.extensions)
  implementation(libs.androidx.paging.common)
  implementation(libs.datastore.core)
  implementation(platform(libs.ktor.bom))
  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.auth)

  testFixturesImplementation(libs.kotlinx.coroutines.core)
  testFixturesImplementation(libs.kotlin.protobuf)
  testFixturesImplementation(libs.androidx.paging.common)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
}
