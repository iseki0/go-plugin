
# GoPlugin

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/iseki0/go-plugin/build.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/space.iseki.go-plugin/go-plugin)](https://central.sonatype.com/artifact/space.iseki.go-plugin/go-plugin)
![License](https://img.shields.io/github/license/iseki0/go-plugin)

A Gradle plugin used to build go program or library.

```kotlin
plugins {
    id("space.iseki.go-plugin")
}

go {
    // For executable
    exec("bin-exec") {
        inputSource = project.layout.projectDirectory.dir("gosrc")
        goTargets(GoTargets.LINUX_AMD64)
        goTargets(GoTargets.LINUX_ARM64)
    }

    // For dynamic library
    dll("bin-dll") {
        inputSource = project.layout.projectDirectory.dir("gosrc")
        // Cross-platform building use Zig toolchain
        goTargets(GoTargets.LINUX_AMD64) {
            environmentProperty.put("CGO_ENABLED", "1")
            environmentProperty.put("CC", "zig cc -target x86_64-linux-gnu")
            environmentProperty.put("CXX", "zig cxx -target x86_64-linux-gnu")
        }
        goTargets(GoTargets.LINUX_ARM64) {
            environmentProperty.put("CGO_ENABLED", "1")
            environmentProperty.put("CC", "zig cc -target aarch64-linux")
            environmentProperty.put("CXX", "zig cxx -target aarch64-linux")
        }
    }

}
```
In `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        gradlePluginPortal()
    }
}
```
