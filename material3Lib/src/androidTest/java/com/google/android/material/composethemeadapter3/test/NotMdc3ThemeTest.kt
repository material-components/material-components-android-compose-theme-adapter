/*
 * Copyright 2021 The Android Open Source Project
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

@file:Suppress("DEPRECATION")

package com.google.android.material.composethemeadapter3.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.MediumTest
import com.google.android.material.composethemeadapter3.Mdc3Theme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(JUnit4::class)
class NotMdc3ThemeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<NotMdc3Activity>()

    @Test(expected = IllegalArgumentException::class)
    fun throwForNonMdc3Theme() = composeTestRule.setContent {
        Mdc3Theme {
            // Nothing to do here, exception should be thrown
        }
    }
}
