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

import android.content.Context
import android.content.res.Resources
import androidx.compose.Composable
import androidx.compose.remember
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.ui.core.ContextAmbient
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Shapes
import androidx.ui.material.Typography
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.unit.Density
import java.lang.reflect.Method

/**
 * A [MaterialTheme] which reads the corresponding values from a
 * Material Design Components Android theme in the given [context].
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [setTextColors] parameter.
 *
 * @param context The context to read the theme from.
 * @param readColors whether the read the MDC color palette from the context's theme.
 * If `false`, the current value of [MaterialTheme.colors] is preserved.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 * If `false`, the current value of [MaterialTheme.typography] is preserved.
 * @param readShapes whether the read the MDC shape appearances from the context's theme.
 * If `false`, the current value of [MaterialTheme.shapes] is preserved.
 * @param setTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`.
 */
@Composable
fun MaterialThemeFromMdcTheme(
    context: Context = ContextAmbient.current,
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    setTextColors: Boolean = false,
    content: @Composable () -> Unit
) {
    // We try and use the theme key value if available, which should be a perfect key for caching
    // and avoid the expensive theme lookups in re-compositions.
    //
    // If the key is not available, we use the Theme itself as a rough approximation. Using the
    // Theme instance as the key is not perfect, but it should work for 90% of cases.
    // It falls down when the theme is manually mutated after a composition has happened
    // (via `applyStyle()`, `rebase()`, `setTo()`), but the majority of apps do not use those.
    val key = context.theme.key ?: context.theme

    val themeParams = remember(key) {
        generateMaterialThemeFromMdcTheme(
            context = context,
            readColors = readColors,
            readTypography = readTypography,
            readShapes = readShapes,
            setTextColors = setTextColors
        )
    }

    MaterialTheme(
        colors = themeParams.colors ?: MaterialTheme.colors,
        typography = themeParams.typography ?: MaterialTheme.typography,
        shapes = themeParams.shapes ?: MaterialTheme.shapes,
        content = content
    )
}

/**
 * This class contains the individual components of a [MaterialTheme]: [ColorPalette], [Typography]
 * and [Shapes].
 */
data class ThemeParameters(
    val colors: ColorPalette?,
    val typography: Typography?,
    val shapes: Shapes?
)

/**
 * This effect generates the components of a [androidx.ui.material.MaterialTheme], reading the
 * values from an Material Design Components Android theme.
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.ui.material.Emphasis] through [androidx.ui.material.ProvideEmphasis].
 * You can customize this through the [setTextColors] parameter.
 *
 * The individual components of the returned [ThemeParameters] may be `null`, depending on the
 * matching 'read' parameter. For example, if you set [readColors] to `false`,
 * [ThemeParameters.colors] will be null.
 *
 * @param context The context to read the theme from.
 * @param density The current density.
 * @param readColors whether the read the MDC color palette from the context's theme.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 * @param readShapes whether the read the MDC shape appearances from the context's theme.
 * @param setTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`.
 * @return [ThemeParameters] instance containing the resulting [ColorPalette], [Typography]
 * and [Shapes].
 */
fun generateMaterialThemeFromMdcTheme(
    context: Context,
    density: Density = Density(context),
    readColors: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    setTextColors: Boolean = false
): ThemeParameters {
    return context.obtainStyledAttributes(R.styleable.ComposeThemeAdapterTheme).use { ta ->
        require(ta.hasValue(R.styleable.ComposeThemeAdapterTheme_isMaterialTheme)) {
            "MaterialThemeUsingMdcTheme requires the host context's theme" +
                " to extend Theme.MaterialComponents"
        }

        val colors: ColorPalette? = if (readColors) {
            /* First we'll read the Material color palette */
            val primary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorPrimary)
            val primaryVariant = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorPrimaryVariant)
            val onPrimary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnPrimary)
            val secondary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSecondary)
            val secondaryVariant = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSecondaryVariant)
            val onSecondary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSecondary)
            val background = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_android_colorBackground)
            val onBackground = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnBackground)
            val surface = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSurface)
            val onSurface = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSurface)
            val error = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorError)
            val onError = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnError)

            val isLightTheme = ta.getBoolean(R.styleable.ComposeThemeAdapterTheme_isLightTheme, true)

            if (isLightTheme) {
                lightColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    secondaryVariant = secondaryVariant,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    error = error,
                    onError = onError
                )
            } else {
                darkColorPalette(
                    primary = primary,
                    primaryVariant = primaryVariant,
                    onPrimary = onPrimary,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    error = error,
                    onError = onError
                )
            }
        } else null

        /**
         * Next we'll generate a typography instance, using the Material Theme text appearances
         * for TextStyles.
         *
         * We create a normal 'empty' instance first to start from the defaults, then merge in our
         * generated text styles from the Android theme.
         */

        val typography = if (readTypography) {
            Typography().merge(
                h1 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline1),
                    setTextColors
                ),
                h2 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline2),
                    setTextColors
                ),
                h3 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline3),
                    setTextColors
                ),
                h4 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline4),
                    setTextColors
                ),
                h5 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline5),
                    setTextColors
                ),
                h6 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadline6),
                    setTextColors
                ),
                subtitle1 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceSubtitle1),
                    setTextColors
                ),
                subtitle2 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceSubtitle2),
                    setTextColors
                ),
                body1 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceBody1),
                    setTextColors
                ),
                body2 = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceBody2),
                    setTextColors
                ),
                button = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceButton),
                    setTextColors
                ),
                caption = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceCaption),
                    setTextColors
                ),
                overline = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceOverline),
                    setTextColors
                )
            )
        } else null

        /**
         * Now read the shape appearances
         */
        val shapes = if (readShapes) {
            Shapes(
                small = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceSmallComponent),
                    fallbackShape = emptyShapes.small
                ),
                medium = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceMediumComponent),
                    fallbackShape = emptyShapes.medium
                ),
                large = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceLargeComponent),
                    fallbackShape = emptyShapes.large
                )
            )
        } else null

        ThemeParameters(colors, typography, shapes)
    }
}

private val emptyShapes = Shapes()

/**
 * This is gross, but we need a way to check for theme equality. Theme does not implement
 * `equals()` or `hashCode()`, but it does have a hidden method called `getKey()`.
 *
 * The cost of this reflective invoke is a lot cheaper than the full theme read which can
 * happen on each re-composition.
 */
private inline val Resources.Theme.key: Any?
    get() {
        if (!sThemeGetKeyMethodFetched) {
            try {
                @Suppress("PrivateApi")
                sThemeGetKeyMethod = Resources.Theme::class.java.getDeclaredMethod("getKey")
                    .apply { isAccessible = true }
            } catch (e: ReflectiveOperationException) {
                // Failed to retrieve Theme.getKey method
            }
            sThemeGetKeyMethodFetched = true
        }
        if (sThemeGetKeyMethod != null) {
            return try {
                sThemeGetKeyMethod?.invoke(this)
            } catch (e: ReflectiveOperationException) {
                // Failed to invoke Theme.getKey()
            }
        }
        return null
    }

private var sThemeGetKeyMethodFetched = false
private var sThemeGetKeyMethod: Method? = null
