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

package com.google.android.material.composethemeadapter.test

import android.view.ContextThemeWrapper
import androidx.annotation.StyleRes
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.test.filters.MediumTest
import androidx.test.filters.SdkSuppress
import com.google.android.material.composethemeadapter.MdcTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(JUnit4::class)
class DefaultFontFamilyMdcThemeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<DefaultFontFamilyMdcActivity>()

    @Test
    @SdkSuppress(maxSdkVersion = 22) // On API 21-22, the family is loaded with only the 400 font
    fun rubik_family_api21() = composeTestRule.setContent {
        val rubik = Font(R.font.rubik, FontWeight.W400).toFontFamily()
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultAndroidFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 23) // XML font families with >1 fonts are only supported on API 23+
    fun rubik_family_api23() = composeTestRule.setContent {
        val rubik = FontFamily(
            Font(R.font.rubik_300, FontWeight.W300),
            Font(R.font.rubik_400, FontWeight.W400),
            Font(R.font.rubik_500, FontWeight.W500),
            Font(R.font.rubik_700, FontWeight.W700),
        )
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultAndroidFontFamily_Rubik) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik)
            }
        }
    }

    @Test
    fun rubik_fixed400() = composeTestRule.setContent {
        val rubik400 = Font(R.font.rubik_400, FontWeight.W400).toFontFamily()
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamily_Rubik400) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik400)
            }
        }
        WithThemeOverlay(R.style.ThemeOverlay_MdcThemeTest_DefaultAndroidFontFamily_Rubik400) {
            MdcTheme(setDefaultFontFamily = true) {
                MaterialTheme.typography.assertFontFamilies(expected = rubik400)
            }
        }
    }

    @Test
    fun rubik_fixed700_withTextAppearances() = composeTestRule.setContent {
        val rubik700 = Font(R.font.rubik_700, FontWeight.W700).toFontFamily()
        WithThemeOverlay(
            R.style.ThemeOverlay_MdcThemeTest_DefaultFontFamilies_Rubik700_WithTextAppearances
        ) {
            MdcTheme {
                MaterialTheme.typography.assertFontFamilies(
                    expected = rubik700,
                    notEquals = true
                )
            }
        }
    }
}

private fun Typography.assertFontFamilies(
    expected: FontFamily,
    notEquals: Boolean = false
) {
    if (notEquals) assertNotEquals(expected, h1.fontFamily) else assertEquals(expected, h1.fontFamily)
    if (notEquals) assertNotEquals(expected, h2.fontFamily) else assertEquals(expected, h2.fontFamily)
    if (notEquals) assertNotEquals(expected, h3.fontFamily) else assertEquals(expected, h3.fontFamily)
    if (notEquals) assertNotEquals(expected, h4.fontFamily) else assertEquals(expected, h4.fontFamily)
    if (notEquals) assertNotEquals(expected, h5.fontFamily) else assertEquals(expected, h5.fontFamily)
    if (notEquals) assertNotEquals(expected, h6.fontFamily) else assertEquals(expected, h6.fontFamily)
    if (notEquals) assertNotEquals(expected, subtitle1.fontFamily) else assertEquals(expected, subtitle1.fontFamily)
    if (notEquals) assertNotEquals(expected, subtitle2.fontFamily) else assertEquals(expected, subtitle2.fontFamily)
    if (notEquals) assertNotEquals(expected, body1.fontFamily) else assertEquals(expected, body1.fontFamily)
    if (notEquals) assertNotEquals(expected, body2.fontFamily) else assertEquals(expected, body2.fontFamily)
    if (notEquals) assertNotEquals(expected, button.fontFamily) else assertEquals(expected, button.fontFamily)
    if (notEquals) assertNotEquals(expected, caption.fontFamily) else assertEquals(expected, caption.fontFamily)
    if (notEquals) assertNotEquals(expected, overline.fontFamily) else assertEquals(expected, overline.fontFamily)
}

/**
 * Function which applies an Android theme overlay to the current context.
 */
@Composable
fun WithThemeOverlay(
    @StyleRes themeOverlayId: Int,
    content: @Composable () -> Unit,
) {
    val themedContext = ContextThemeWrapper(LocalContext.current, themeOverlayId)
    CompositionLocalProvider(LocalContext provides themedContext, content = content)
}
