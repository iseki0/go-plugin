plugins {
    `java-gradle-plugin`
    id("java")
    publishing
    signing
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.14.0"
    id("com.gradle.plugin-publish") version "1.2.1"
}

val projectUrl = "https://github.com/iseki0/goplugin"

allprojects {
    group = "space.iseki.go-plugin"
    version = "0.1-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "Central"
            url = if (project.version.toString().endsWith("SNAPSHOT")) {
                uri("https://oss.sonatype.org/content/repositories/snapshots")
            } else {
                uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }
            credentials {
                username = properties["ossrhUsername"]?.toString() ?: System.getenv("OSSRH_USERNAME")
                password = properties["ossrhPassword"]?.toString() ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}


afterEvaluate {
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    signing {
        // To use local gpg command, configure gpg options in ~/.gradle/gradle.properties
        // reference: https://docs.gradle.org/current/userguide/signing_plugin.html#example_configure_the_gnupgsignatory
        useGpgCmd()
        for (it in publishing.publications) {
            sign(it)
        }
    }
}

gradlePlugin {
    website = projectUrl
    vcsUrl = projectUrl
    plugins {
        create("goPlugin") {
            id = "space.iseki.go-plugin"
            implementationClass = "space.iseki.goplugin.GoCompilePlugin"
            displayName = "go-plugin"
            description = "Utils plugin for go build and bundle into Jar file"
            tags = listOf("go")
        }
    }
    publishing {
        afterEvaluate {
            publications.configureEach {
                this as MavenPublication
                pom {
                    name = project.name
                    description = project.description
                    url = projectUrl
                    licenses {
                        license {
                            name = "Apache-2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0"
                        }
                    }
                    developers {
                        developer {
                            id = "iseki0"
                            name = "iseki zero"
                            email = "admin@iseki.space"
                        }
                    }
                    scm {
                        connection = "scm:git:$projectUrl.git"
                        developerConnection = connection
                        url = projectUrl
                    }
                }
            }
        }
    }
}
