/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

version = project.property("firebase-functions.version") as String

plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    compileSdkVersion(property("targetSdkVersion") as Int)
    defaultConfig {
        minSdkVersion(property("minSdkVersion") as Int)
        targetSdkVersion(property("targetSdkVersion") as Int)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    packagingOptions {
        pickFirst("META-INF/kotlinx-serialization-core.kotlin_module")
        pickFirst("META-INF/AL2.0")
        pickFirst("META-INF/LGPL2.1")
    }
    lintOptions {
        isAbortOnError = false
    }
}

kotlin {

    android {
        publishAllLibraryVariants()
    }

    fun nativeTargetConfig(): KotlinNativeTarget.() -> Unit = {
        val nativeFrameworkPaths = listOf(
            rootProject.project("firebase-app").projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/iOS"),
            projectDir.resolve("src/nativeInterop/cinterop/Carthage/Build/iOS")
        )

        binaries {
            getTest("DEBUG").apply {
                linkerOpts(nativeFrameworkPaths.map { "-F$it" })
                linkerOpts("-ObjC")
            }
        }

        compilations.getByName("main") {
            cinterops.create("FirebaseFunctions") {
                compilerOpts(nativeFrameworkPaths.map { "-F$it" })
                extraOpts("-verbose")
            }
        }
    }

    if (project.extra["ideaActive"] as Boolean) {
        iosX64("ios", nativeTargetConfig())
    } else {
        ios(configure = nativeTargetConfig())
    }

    js {
        useCommonJs()
        nodejs {
            testTask {
                useMocha {
                    timeout = "5s"
                }
            }
        }
        browser {
            testTask {
                useMocha {
                    timeout = "5s"
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                apiVersion = "1.4"
                languageVersion = "1.4"
                progressiveMode = true
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-functions:19.2.0")
            }
        }

        val iosMain by getting

        val jsMain by getting
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
