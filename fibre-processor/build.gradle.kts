plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnlyApi("org.jspecify:jspecify:1.0.0")

    implementation("com.palantir.javapoet:javapoet:0.22.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}