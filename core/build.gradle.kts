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
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}
