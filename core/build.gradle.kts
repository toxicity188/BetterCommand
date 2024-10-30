plugins {
    `maven-publish`
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.11.0")
    compileOnly("net.kyori:adventure-api:4.17.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}