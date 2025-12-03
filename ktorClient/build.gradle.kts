import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "io.github.hussein-al-zuhile"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    kotlin("plugin.serialization")
}

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    jvm()
    androidLibrary {
        namespace = "io.github.husseinAlZuhile.kmpKtorClient"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlin.logging)

            // Ktor
            implementation(libs.ktor.client.core)
            api(libs.ktor.client.resources)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.content.negotiation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.client.mock)
        }
    }
}


publishing {

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/hussein-al-zuhile/KMP-Ktor-Client")
        }
    }
}


mavenPublishing {
    version = "1.0.1"
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "kmp-ktor-client", version.toString())

    pom {
        name = "KMP - Ktor - Client"
        description = "A Ktor client for kotlin multiplatform using resources (Type-safe Requests)"
        inceptionYear = "2025"
        url = "https://github.com/Hussein-Al-Zuhile/KMP-Ktor-Client"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "Hussein-Al-Zuhile"
                name = "Hussein Al-Zuhile"
                url = "https://github.com/Hussein-Al-Zuhile/"
                email = "hosenzuh@gmail.com"
                organization = "Hussein Al-Zuhile"
                organizationUrl = "https://github.com/Hussein-Al-Zuhile/"
            }
        }
        scm {
            url = "https://github.com/Hussein-Al-Zuhile/KMP-Ktor-Client"
            connection = "scm:git:git://github.com/Hussein-Al-Zuhile/KMP-Ktor-Client.git"
            developerConnection = "scm:git:ssh://git@github.com/Hussein-Al-Zuhile/KMP-Ktor-Client.git"
        }
    }
}
