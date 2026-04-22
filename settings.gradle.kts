// Configurazione Gradle a livello di progetto.
// Qui dichiariamo dove cercare i plugin e le librerie.

pluginManagement {
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
    // FAIL_ON_PROJECT_REPOS: se un modulo dichiara repository locali, Gradle fallisce.
    // Serve a tenere tutto centralizzato qui — niente sorprese.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Jarvis"
include(":app")
