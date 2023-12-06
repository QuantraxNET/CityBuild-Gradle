import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" // Generates the plugin.yml when building the project
    id ("com.github.johnrengelman.shadow") version "8.0.0"

}

group = "net.quantrax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven ("https://nexus.derioo.de/nexus/content/repositories/CommandFramework")
    maven ("https://nexus.derioo.de/nexus/content/repositories/InventoryFramwork")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    implementation ("de.derioo:CommandFramework:3.1.4-RELEASE")
    implementation ("de.derioo:InventoryFramwork:6.1.5")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "net.quantrax.citybuild.CityBuildPlugin"))
        }
    }
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
