import space.iseki.goplugin.GoTargets

plugins {
    `java`
    id("space.iseki.goplugin")
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

go {
    dll("aa") {
        inputSource = project.layout.buildDirectory.dir("../go")
        goTargets(GoTargets.WINDOWS_AMD64) {
            environmentProperty.put("CGO_ENABLED", "1")
            environmentProperty.put("CC", "zig cc -target x86_64-windows")
            environmentProperty.put("CXX", "zig cxx -target x86_64-windows")
        }
        goTargets(GoTargets.LINUX_AMD64) {
            environmentProperty.put("CGO_ENABLED", "1")
            environmentProperty.put("CC", "zig cc -target x86_64-linux-gnu")
            environmentProperty.put("CXX", "zig cxx -target x86_64-linux-gnu")
        }
    }
}

application{
    mainClass = "Main"
}
