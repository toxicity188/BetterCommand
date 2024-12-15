plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")

    group = "kr.toxicity.command"
    version = "1.4.1"

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")

        testCompileOnly("org.projectlombok:lombok:1.18.34")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.34")

        compileOnly("com.mojang:brigadier:1.3.10")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }
    }

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(17)
    }
}