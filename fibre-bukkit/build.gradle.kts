plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.1"
}

dependencies {
    api(project(":fibre-core"))
    runtimeOnly(project(":fibre-bukkit-legacy"))
    runtimeOnly(project(":fibre-bukkit-paper"))
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

tasks.runServer {
    minecraftVersion("1.21.11")
}

tasks.shadowJar {
    archiveBaseName.set("Fibre")
    archiveClassifier.set("")
    dependencies {
        include(project(":fibre-core"))
        include(project(":fibre-bukkit-legacy"))
        include(project(":fibre-bukkit-paper"))
    }
}

bukkitPluginYaml {
    name = "Fibre"
    description.set(rootProject.description)
    website.set("https://github.com/Reasonlesss/fibre")
    main.set("codes.reason.fibre.bukkit.FibreBukkitPlugin")
    authors.add("Reasonless")
}
