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

package com.google.android.material.composethemeadapter3

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.test.filters.MediumTest
import com.google.android.material.composethemeadapter3.test.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@MediumTest
@RunWith(JUnit4::class)
class Mdc3ThemeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<Mdc3Activity>()

    @Test
    fun colors() = composeTestRule.setContent {
        Mdc3Theme {
            val colorScheme = MaterialTheme.colorScheme

            Assert.assertEquals(colorResource(R.color.aquamarine), colorScheme.primary)
            Assert.assertEquals(colorResource(R.color.pale_turquoise), colorScheme.onPrimary)
            Assert.assertEquals(colorResource(R.color.midnight_blue), colorScheme.inversePrimary)
            Assert.assertEquals(colorResource(R.color.royal_blue), colorScheme.primaryContainer)
            Assert.assertEquals(colorResource(R.color.steel_blue), colorScheme.onPrimaryContainer)

            Assert.assertEquals(colorResource(R.color.dodger_blue), colorScheme.secondary)
            Assert.assertEquals(colorResource(R.color.dark_golden_rod), colorScheme.onSecondary)
            Assert.assertEquals(colorResource(R.color.peru), colorScheme.secondaryContainer)
            Assert.assertEquals(colorResource(R.color.blue_violet), colorScheme.onSecondaryContainer)

            Assert.assertEquals(colorResource(R.color.dark_orchid), colorScheme.tertiary)
            Assert.assertEquals(colorResource(R.color.slate_gray), colorScheme.onTertiary)
            Assert.assertEquals(colorResource(R.color.gray), colorScheme.tertiaryContainer)
            Assert.assertEquals(colorResource(R.color.spring_green), colorScheme.onTertiaryContainer)

            Assert.assertEquals(colorResource(R.color.medium_spring_green), colorScheme.background)
            Assert.assertEquals(colorResource(R.color.navy), colorScheme.onBackground)

            Assert.assertEquals(colorResource(R.color.dark_blue), colorScheme.surface)
            Assert.assertEquals(colorResource(R.color.light_coral), colorScheme.onSurface)
            Assert.assertEquals(colorResource(R.color.salmon), colorScheme.surfaceVariant)
            Assert.assertEquals(colorResource(R.color.dark_salmon), colorScheme.onSurfaceVariant)
            Assert.assertEquals(colorResource(R.color.light_salmon), colorScheme.inverseSurface)
            Assert.assertEquals(colorResource(R.color.orchid), colorScheme.inverseOnSurface)

            Assert.assertEquals(colorResource(R.color.violet), colorScheme.outline)

            Assert.assertEquals(colorResource(R.color.beige), colorScheme.error)
            Assert.assertEquals(colorResource(R.color.white_smoke), colorScheme.onError)
            Assert.assertEquals(colorResource(R.color.olive), colorScheme.errorContainer)
            Assert.assertEquals(colorResource(R.color.olive_drab), colorScheme.onErrorContainer)

            // Mdc3Theme updates the LocalContentColor to match the calculated onBackground
            Assert.assertEquals(colorResource(R.color.navy), LocalContentColor.current)
        }
    }

    @Test
    fun type() = composeTestRule.setContent {
        Mdc3Theme {
            val typography = MaterialTheme.typography
            val density = LocalDensity.current

            val rubik = FontFamily(
                Font(resId = R.font.rubik_300, weight = FontWeight.W300),
                Font(resId = R.font.rubik_400, weight = FontWeight.W400),
                Font(resId = R.font.rubik_500, weight = FontWeight.W500),
                Font(resId = R.font.rubik_700, weight = FontWeight.W700)
            )
            val rubik300 = Font(R.font.rubik_300).toFontFamily()
            val rubik400 = Font(R.font.rubik_400).toFontFamily()
            val sansSerif = FontFamilyWithWeight(FontFamily.SansSerif)
            val sansSerifLight = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            val sansSerifBlack = FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            val serif = FontFamilyWithWeight(FontFamily.Serif)
            val cursive = FontFamilyWithWeight(FontFamily.Cursive)
            val monospace = FontFamilyWithWeight(FontFamily.Monospace)

            typography.displayLarge.run {
                assertTextUnitEquals(97.54.sp, fontSize, density)
                assertTextUnitEquals((-0.0015).em, letterSpacing, density)
                Assert.assertEquals(rubik300, fontFamily)
            }

            Assert.assertNotNull(typography.displayMedium.shadow)
            typography.displayMedium.shadow!!.run {
                Assert.assertEquals(colorResource(R.color.olive_drab), color)
                Assert.assertEquals(4.43f, offset.x)
                Assert.assertEquals(8.19f, offset.y)
                Assert.assertEquals(2.13f, blurRadius)
            }

            typography.displaySmall.run {
                Assert.assertEquals(sansSerif.fontFamily, fontFamily)
                Assert.assertEquals(sansSerif.weight, fontWeight)
            }

            typography.headlineLarge.run {
                Assert.assertEquals(sansSerifLight.fontFamily, fontFamily)
                Assert.assertEquals(sansSerifLight.weight, fontWeight)
            }

            typography.headlineMedium.run {
                Assert.assertEquals(sansSerifLight.fontFamily, fontFamily)
                Assert.assertEquals(sansSerifLight.weight, fontWeight)
            }

            typography.headlineSmall.run {
                Assert.assertEquals(sansSerifBlack.fontFamily, fontFamily)
                Assert.assertEquals(sansSerifBlack.weight, fontWeight)
            }

            typography.titleLarge.run {
                Assert.assertEquals(serif.fontFamily, fontFamily)
                Assert.assertEquals(serif.weight, fontWeight)
            }

            typography.titleMedium.run {
                Assert.assertEquals(monospace.fontFamily, fontFamily)
                Assert.assertEquals(monospace.weight, fontWeight)
                assertTextUnitEquals(0.em, letterSpacing, density)
            }

            typography.titleSmall.run {
                Assert.assertEquals(FontFamily.SansSerif, fontFamily)
            }

            typography.bodyLarge.run {
                assertTextUnitEquals(16.26.sp, fontSize, density)
                assertTextUnitEquals(0.005.em, letterSpacing, density)
                Assert.assertEquals(rubik400, fontFamily)
                Assert.assertNull(shadow)
            }

            typography.bodyMedium.run {
                Assert.assertEquals(cursive.fontFamily, fontFamily)
                Assert.assertEquals(cursive.weight, fontWeight)
            }

            typography.bodySmall.run {
                Assert.assertEquals(FontFamily.SansSerif, fontFamily)
                assertTextUnitEquals(0.04.em, letterSpacing, density)
            }

            typography.labelLarge.run {
                Assert.assertEquals(rubik, fontFamily)
            }

            typography.labelMedium.run {
                Assert.assertEquals(rubik, fontFamily)
            }

            typography.labelSmall.run {
                Assert.assertEquals(FontFamily.SansSerif, fontFamily)
            }
        }
    }
}

private fun assertTextUnitEquals(expected: TextUnit, actual: TextUnit, density: Density) {
    if (expected.javaClass == actual.javaClass) {
        // If the expected and actual are the same type, compare the raw values with a
        // delta to account for float inaccuracy
        Assert.assertEquals(expected.value, actual.value, 0.001f)
    } else {
        // Otherwise we need to flatten to a px to compare the values. Again using a
        // delta to account for float inaccuracy
        with(density) { Assert.assertEquals(expected.toPx(), actual.toPx(), 0.001f) }
    }
}
