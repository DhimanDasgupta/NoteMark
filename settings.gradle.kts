pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "NoteMark"

include(":app")

include(":common-core")

include(":common-android")

include(":common-compose")

include(":common-data")

include(":common-state-event-api")

include(":common-navigation-api")

include(":common-state-event-impl")

include(":common-presenter")

include(":feat-launcher")

include(":feat-login")

include(":feat-registration")

include(":feat-notelist")
