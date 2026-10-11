plugins {
  alias(libs.plugins.notemark.android.library)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.sqlDelight)
  alias(libs.plugins.google.protobuf)
  alias(libs.plugins.metro)
}

android {
  namespace = "com.dhimandasgupta.notemark.data"
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
  implementation(libs.ktor.client.contentNegotiation)
  implementation(libs.ktor.serialization.kotlinx.json)
  implementation(libs.ktor.client.logging)

  // junit and kotlin("test") come from the library convention plugin.
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.ktor.client.mock)
}
