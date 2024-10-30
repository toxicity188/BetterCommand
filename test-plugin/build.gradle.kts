plugins {
    id("io.papermc.paperweight.userdev") version "1.7.4"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0"
    id("io.github.goooler.shadow") version "8.1.8"
}

val library = rootProject.project("core")

val minecraft = "1.20.4"

dependencies {
    paperweight.paperDevBundle("$minecraft-R0.1-SNAPSHOT")
    implementation(library)
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    shadowJar {
        archiveClassifier = ""
    }
    runServer {
        version(minecraft)
    }
}

bukkitPluginYaml {
    main = "kr.toxicity.command.test.CommandTest"
    apiVersion = "1.20"
    name = "CommandTest"

    author = "toxicity"
    description = "command text plugin."
}