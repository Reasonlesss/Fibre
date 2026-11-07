
plugins {
    `java-library`
//    id("com.diffplug.spotless") version "8.9.0" apply false
}

description = "A framework for creating and managing Minecraft minigames."

allprojects {
    pluginManager.apply("java-library")

    group = "cloud.emilys.fibre"
    version = "0.1.0-SNAPSHOT"

    dependencies {
        compileOnlyApi("org.jspecify:jspecify:1.0.0")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    }

//    pluginManager.withPlugin("java") {
//        extensions.configure<SpotlessExtension> {
//            java {
//                palantirJavaFormat("2.97.0")
//            }
//        }
//    }
}
