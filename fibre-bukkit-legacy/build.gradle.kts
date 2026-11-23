plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":fibre-bukkit"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
}
