plugins {
    java
}

dependencies {
    compileOnly(project(":fibre-plugin"))
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}
