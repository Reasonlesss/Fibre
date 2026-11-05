plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnlyApi("org.jspecify:jspecify:1.0.0")
    compileOnlyApi(project(":fibre-api"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
