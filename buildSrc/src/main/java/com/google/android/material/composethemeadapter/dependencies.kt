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
    const val ktlint = "0.43.2"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.0.4"

    const val gradleMavenPublishPlugin = "com.vanniktech:gradle-maven-publish-plugin:0.18.0"

    const val mdc = "com.google.android.material:material:1.6.0-alpha01"

    object Kotlin {
        const val version = "1.6.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"

        const val binaryCompatibility = "org.jetbrains.kotlinx:binary-compatibility-validator:0.8.0"
    }

    object Dokka {
        const val gradlePlugin = "org.jetbrains.dokka:dokka-gradle-plugin:1.6.10"
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

            const val version = "1.1.0-rc01"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"

            const val ui = "androidx.compose.ui:ui:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val material3 = "androidx.compose.material3:material3:1.0.0-alpha02"

            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val test = "androidx.compose.ui:ui-test-junit4:${version}"
        }

        const val appcompat = "androidx.appcompat:appcompat:1.4.0"

        const val coreKtx = "androidx.core:core-ktx:1.7.0"

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.4.0"
        }

        object Test {
            private const val version = "1.4.0"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            const val ext = "androidx.test.ext:junit:1.1.3"

            const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
        }
    }

    const val junit = "junit:junit:4.13.2"

    const val truth = "com.google.truth:truth:1.1.3"
}
