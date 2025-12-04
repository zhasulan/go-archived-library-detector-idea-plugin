plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.github.yourname.goarchived"
version = "1.0.2"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    intellijPlatform {
        goland("2024.2.5")
        bundledPlugin("org.jetbrains.plugins.go")

        pluginVerifier()
        zipSigner()
        instrumentationTools()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")      // чтобы видеть org.junit.Assert и т.п.
    testRuntimeOnly("junit:junit:4.13.2")         // нужно рантайму IntelliJ test framework
    testImplementation("org.opentest4j:opentest4j:1.3.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "252.*"
        }
    }

    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.10"
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}