import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish") version "0.30.0"
    signing
}

java {
    withSourcesJar()
    withJavadocJar()
}

signing {
    useGpgCmd()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.12.1")
    compileOnly("net.kyori:adventure-api:4.18.0")
}

mavenPublishing  {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates("io.github.toxicity188", rootProject.name, project.version as String)
    configure(JavaLibrary(
        javadocJar = JavadocJar.None(),
        sourcesJar = true,
    ))
    pom {
        name = rootProject.name
        description = "cross-platform command library implementing brigadier, adventure."
        inceptionYear = "2024"
        url = "https://github.com/toxicity188/BetterCommand/"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit-license.org/"
            }
        }
        developers {
            developer {
                id = "toxicity188"
                name = "toxicity188"
                url = "https://github.com/toxicity188/"
            }
        }
        scm {
            url = "https://github.com/toxicity188/BetterCommand/"
            connection = "scm:git:git://github.com/toxicity188/BetterCommand.git"
            developerConnection = "scm:git:ssh://git@github.com/toxicity188/BetterCommand.git"
        }
    }
}