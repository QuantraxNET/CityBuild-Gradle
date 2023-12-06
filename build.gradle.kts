import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates the plugin.yml when building the project
}

group = "net.quantrax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

bukkit {
    main = "net.quantrax.citybuild.CityBuildPlugin"
    foliaSupported = false
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    author = "MerryChrismas"
    contributors = listOf("ExceptionThread", "DeRio_", "ByTRYO")

    commands {
        /*
        Register your commands here.

        register("test") {
            description = "This is a test command!"
            aliases = listOf("t")
        }
         */
    }
}
