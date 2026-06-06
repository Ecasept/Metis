@file:Suppress("UnstableApiUsage")
rootProject.name = "todo-uni-augsburg"
include("shared", "server", "client")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}