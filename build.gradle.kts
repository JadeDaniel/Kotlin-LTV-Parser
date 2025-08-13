
plugins {
    application
    kotlin("jvm") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
}

group = "co.couldbe.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(24)
}

application {
    mainClass.set("co.couldbe.demo.MainKt")
}
