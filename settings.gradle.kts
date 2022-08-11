pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "service-loader"
include(":app")
include(":service-loader-annotation")
include(":service-loader-compiler")
include(":service-loader-api")
include(":apple-api")
include(":apple")
include(":banana-api")
include(":banana")
 