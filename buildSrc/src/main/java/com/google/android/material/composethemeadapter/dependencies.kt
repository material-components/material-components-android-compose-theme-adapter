/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.composethemeadapter

object Versions {
    const val ktlint = "0.40.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.3.0"

    const val gradleMavenPublishPlugin = "com.vanniktech:gradle-maven-publish-plugin:0.13.0"

    object Kotlin {
        const val version = "1.7.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

        const val binaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.3.0"
    }

    object Dokka {
        const val gradlePlugin = "org.jetbrains.dokka:dokka-gradle-plugin:1.5.0"
    }

    object AndroidX {
        object Compose {
            const val snapshot = ""

            @JvmStatic
            val snapshotUrl: String
                get() = when {
                    snapshot.isNotEmpty() -> {
                        "https://androidx.dev/snapshots/builds/$snapshot/artifacts/repository/"
                    }
                    else -> throw IllegalArgumentException("Snapshot version not set")
                }

            const val version = "1.2.1"
            const val compilerVersion = "1.3.1"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"

            const val ui = "androidx.compose.ui:ui:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val material3 = "androidx.compose.material3:material3:1.0.0-beta02"

            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val test = "androidx.compose.ui:ui-test-junit4:${version}"
        }

        const val appcompat = "androidx.appcompat:appcompat:1.5.1"

        const val coreKtx = "androidx.core:core-ktx:1.9.0"

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.5.1"
        }

        object Test {
            private const val version = "1.4.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val ext = "androidx.test.ext:junit:1.1.3"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
        }
    }

    const val junit = "junit:junit:4.13"

    const val truth = "com.google.truth:truth:1.0.1"
}
