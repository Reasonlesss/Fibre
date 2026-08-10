import xyz.jpenilla.resourcefactory.bukkit.bukkitPluginYaml

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("xyz.jpenilla.resource-factory") version "1.3.1"
}

dependencies {
    api(project(":fibre-api"))
    runtimeOnly(project(":fibre-plugin-legacy"))
    runtimeOnly(project(":fibre-plugin-modern"))
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}

tasks.runServer {
    minecraftVersion("1.21.11")
}

tasks.shadowJar {
    archiveBaseName.set("Fibre")
    archiveClassifier.set("")

    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    mergeServiceFiles()
    dependencies {
        include(project(":fibre-api"))
        include(project(":fibre-plugin-legacy"))
        include(project(":fibre-plugin-modern"))
    }
}

bukkitPluginYaml {
    name = "Fibre"
    description.set(rootProject.description)
    website.set("https://github.com/Reasonlesss/fibre")
    main.set("cloud.emilys.fibre.FibrePlugin")
    authors.add("Reasonless")
}
