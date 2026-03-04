plugins {
    kotlin("jvm") version "2.3.0"
    application
    `maven-publish`
}

group = "org.guy.library"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(kotlin("test"))
}

kotlin {
    // Java 11 is highly recommended for maximum compatibility with Android apps
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.guy.library.MainKt")
}

// Configures Maven Publishing so JitPack can build and distribute it
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "org.guy.library"
            artifactId = "aiapi"
            version = "1.0.0"
        }
    }
}