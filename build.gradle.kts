plugins {
    id("java")
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.22"
}

group = "space.iseki.goplugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("goPlugin") {
            id = "space.iseki.goplugin"
            implementationClass = "space.iseki.goplugin.GoCompilePlugin"
        }
    }
}
