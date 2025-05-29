import org.gradle.internal.declarativedsl.parsing.main

plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

val ghPackageUsername: String by project
val ghPackagesPwd: String by project

tasks.register("TestTask") {
    val username = project.findProperty("ghPackagesUsername")?.toString() ?: ghPackageUsername
    val password = project.findProperty("ghPackagesPwd")?.toString() ?: ghPackagesPwd
    println("$username - $password")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://git.informatik.uni-hamburg.de/api/v4/groups/sane-public/-/packages/maven")
    }
    maven {
        url = uri("https://maven.pkg.github.com/Web-of-Digital-Twins/wldt-wodt-adapter")
        credentials {
            username = project.findProperty("ghPackagesUsername")?.toString() ?: ghPackageUsername
            password = project.findProperty("ghPackagesPwd")?.toString() ?: ghPackagesPwd
        }
    }
}

dependencies {
    implementation("io.github.wldt:wldt-core:0.4.0")
    implementation ("io.github.wldt:http-digital-adapter:0.2")
    implementation("io.github.wldt:mqtt-physical-adapter:0.1.2")
    implementation("io.github.wldt:mqtt-digital-adapter:0.1.2")
    implementation(libs.wodt.wldt)
    // https://mvnrepository.com/artifact/mysql/mysql-connector-java
    implementation("mysql:mysql-connector-java:8.0.33")
    // SnakeYAML YAML parser
    implementation("org.yaml:snakeyaml:2.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}