plugins {
    `maven-publish`
}

tasks.jar {
    archiveBaseName = rootProject.name
}

val sourcesJar by tasks.creating(Jar::class.java) {
    from(sourceSets.main.get().allSource)
    archiveClassifier = "sources"
    archiveBaseName = rootProject.name
}

val javadocJar by tasks.creating(Jar::class.java) {
    dependsOn(tasks.javadoc)
    archiveClassifier = "javadoc"
    from(tasks.javadoc)
    archiveBaseName = rootProject.name
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-api:4.17.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name = "BetterCommand"
                description = "A translatable command API that implements brigadier command node."
                url = "https://github.com/toxicity188/BetterCommand"
                licenses {
                    license {
                        name = "MIT license"
                        url = "https://mit-license.org/"
                    }
                }
                developers {
                    developer {
                        id = "toxicity188"
                        email = "angryko@naver.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/toxicity188/BetterCommand.git"
                    developerConnection = "scm:git:ssh://git@github.com:toxicity188/BetterCommand.git"
                    url = "https://github.com/toxicity188/BetterCommand"
                }
            }
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}
