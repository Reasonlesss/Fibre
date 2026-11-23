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
    "fibre-processor",
    "fibre-core",
    "fibre-bukkit",
    "fibre-bukkit-legacy",
    "fibre-bukkit-paper"
)
