import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless") version "8.9.0" apply false
}

description = "A framework for creating and managing Minecraft minigames."

allprojects {
    pluginManager.apply("com.diffplug.spotless")

    group = "cloud.emilys.fibre"
    version = "0.1.0-SNAPSHOT"

    pluginManager.withPlugin("java") {
        extensions.configure<SpotlessExtension> {
            java {
                palantirJavaFormat("2.97.0")
            }
        }
    }
}
