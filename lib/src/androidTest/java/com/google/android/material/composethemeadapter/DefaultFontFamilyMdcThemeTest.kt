package com.google.android.material.composethemeadapter

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
import com.google.android.material.composethemeadapter.test.R
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
  h1.fontFamily.assertFontFamily(expected, notEquals)
  h2.fontFamily.assertFontFamily(expected, notEquals)
  h3.fontFamily.assertFontFamily(expected, notEquals)
  h4.fontFamily.assertFontFamily(expected, notEquals)
  h5.fontFamily.assertFontFamily(expected, notEquals)
  h6.fontFamily.assertFontFamily(expected, notEquals)
  subtitle1.fontFamily.assertFontFamily(expected, notEquals)
  subtitle2.fontFamily.assertFontFamily(expected, notEquals)
  body1.fontFamily.assertFontFamily(expected, notEquals)
  body2.fontFamily.assertFontFamily(expected, notEquals)
  button.fontFamily.assertFontFamily(expected, notEquals)
  caption.fontFamily.assertFontFamily(expected, notEquals)
  overline.fontFamily.assertFontFamily(expected, notEquals)
}

private fun FontFamily?.assertFontFamily(
  expected: FontFamily,
  notEquals: Boolean = false
) {
  if (notEquals) {
    assertNotEquals(expected, this)
  } else {
    assertEquals(expected, this)
  }
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
