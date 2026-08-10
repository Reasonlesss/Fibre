rootProject.name = "fibre"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven(url = "https://hub.spigotmc.org/nexus/content/groups/public/")
    }
}

include(
    "fibre-api",
    "fibre-plugin",
    "fibre-plugin-legacy",
    "fibre-plugin-modern"
)
