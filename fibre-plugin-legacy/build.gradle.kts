plugins {
    java
}

dependencies {
    compileOnly(project(":fibre-plugin"))
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:5.2.0")
}
