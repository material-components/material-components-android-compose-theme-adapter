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

@file:JvmName("Mdc3Theme")
package com.google.android.material.composethemeadapter3

import android.content.Context
import android.content.res.Resources
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import java.lang.reflect.Method

/**
 * A [MaterialTheme] which reads the corresponding values from a Material Components for Android
 * theme in the given [context].
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.compose.material.ContentAlpha] through [androidx.compose.material.LocalContentAlpha].
 * You can customize this through the [setTextColors] parameter.
 *
 * @param context The context to read the theme from.
 * @param readColorScheme whether the read the MDC color palette from the [context]'s theme.
 * If `false`, the current value of [MaterialTheme.colorScheme] is preserved.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 * If `false`, the current value of [MaterialTheme.typography] is preserved.
 * @param readShapes whether the read the MDC shape appearances from the [context]'s theme.
 * If `false`, the current value of [MaterialTheme.shapes] is preserved.
 * @param setTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`.
 * @param setDefaultFontFamily whether to read and prioritize the `fontFamily` attributes from
 * [context]'s theme, over any specified in the MDC text appearances. Defaults to `false`.
 */
@Composable
fun Mdc3Theme(
    context: Context = LocalContext.current,
    readColorScheme: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    setTextColors: Boolean = false,
    setDefaultFontFamily: Boolean = false,
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

    val layoutDirection = LocalLayoutDirection.current

    val themeParams = remember(key) {
        createMdc3Theme(
            context = context,
            layoutDirection = layoutDirection,
            readColorScheme = readColorScheme,
            readTypography = readTypography,
            readShapes = readShapes,
            setTextColors = setTextColors,
            setDefaultFontFamily = setDefaultFontFamily
        )
    }

    MaterialTheme(
        colorScheme = themeParams.colorScheme ?: MaterialTheme.colorScheme,
        typography = themeParams.typography ?: MaterialTheme.typography,
        shapes = themeParams.shapes ?: MaterialTheme.shapes
    ) {
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}

/**
 * This class contains the individual components of a [MaterialTheme]: [ColorScheme] and
 * [Typography].
 */
data class Theme3Parameters(
    val colorScheme: ColorScheme?,
    val typography: Typography?,
    val shapes: Shapes?
)

/**
 * This function creates the components of a [androidx.compose.material.MaterialTheme], reading the
 * values from an Material Components for Android theme.
 *
 * By default the text colors from any associated `TextAppearance`s from the theme are *not* read.
 * This is because setting a fixed color in the resulting [TextStyle] breaks the usage of
 * [androidx.compose.material.ContentAlpha] through [androidx.compose.material.LocalContentAlpha].
 * You can customize this through the [setTextColors] parameter.
 *
 * For [Shapes], the [layoutDirection] is taken into account when reading corner sizes of
 * `ShapeAppearance`s from the theme. For example, [Shapes.medium.topStart] will be read from
 * `cornerSizeTopLeft` for [LayoutDirection.Ltr] and `cornerSizeTopRight` for [LayoutDirection.Rtl].
 *
 * The individual components of the returned [Theme3Parameters] may be `null`, depending on the
 * matching 'read' parameter. For example, if you set [readColorScheme] to `false`,
 * [Theme3Parameters.colors] will be null.
 *
 * @param context The context to read the theme from.
 * @param layoutDirection The layout direction to be used when reading shapes.
 * @param density The current density.
 * @param readColorScheme whether the read the MDC color palette from the [context]'s theme.
 * @param readTypography whether the read the MDC text appearances from [context]'s theme.
 * @param readShapes whether the read the MDC shape appearances from the [context]'s theme.
 * @param setTextColors whether to read the colors from the `TextAppearance`s associated from the
 * theme. Defaults to `false`.
 * @param setDefaultFontFamily whether to read and prioritize the `fontFamily` attributes from
 * [context]'s theme, over any specified in the MDC text appearances. Defaults to `false`.
 * @return [Theme3Parameters] instance containing the resulting [ColorScheme] and [Typography].
 */
fun createMdc3Theme(
    context: Context,
    layoutDirection: LayoutDirection,
    density: Density = Density(context),
    readColorScheme: Boolean = true,
    readTypography: Boolean = true,
    readShapes: Boolean = true,
    setTextColors: Boolean = false,
    setDefaultFontFamily: Boolean = false
): Theme3Parameters {
    return context.obtainStyledAttributes(R.styleable.ComposeThemeAdapterTheme).use { ta ->
        require(ta.hasValue(R.styleable.ComposeThemeAdapterTheme_isMaterial3Theme)) {
            "createMdc3Theme requires the host context's theme to extend Theme.Material3"
        }

        val colorScheme: ColorScheme? = if (readColorScheme) {
            /* First we'll read the Material 3 color palette */
            val primary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorPrimary)
            val onPrimary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnPrimary)
            val primaryInverse = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorPrimaryInverse)
            val primaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorPrimaryContainer)
            val onPrimaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnPrimaryContainer)
            val secondary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSecondary)
            val onSecondary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSecondary)
            val secondaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSecondaryContainer)
            val onSecondaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSecondaryContainer)
            val tertiary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorTertiary)
            val onTertiary = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnTertiary)
            val tertiaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorTertiaryContainer)
            val onTertiaryContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnTertiaryContainer)
            val background = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_android_colorBackground)
            val onBackground = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnBackground)
            val surface = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSurface)
            val onSurface = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSurface)
            val surfaceVariant = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSurfaceVariant)
            val onSurfaceVariant = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSurfaceVariant)
            val elevationOverlay = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_elevationOverlayColor)
            val surfaceInverse = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorSurfaceInverse)
            val onSurfaceInverse = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnSurfaceInverse)
            val outline = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOutline)
            val error = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorError)
            val onError = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnError)
            val errorContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorErrorContainer)
            val onErrorContainer = ta.getComposeColor(R.styleable.ComposeThemeAdapterTheme_colorOnErrorContainer)

            val isLightTheme = ta.getBoolean(R.styleable.ComposeThemeAdapterTheme_isLightTheme, true)

            if (isLightTheme) {
                lightColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer
                )
            } else {
                darkColorScheme(
                    primary = primary,
                    onPrimary = onPrimary,
                    inversePrimary = primaryInverse,
                    primaryContainer = primaryContainer,
                    onPrimaryContainer = onPrimaryContainer,
                    secondary = secondary,
                    onSecondary = onSecondary,
                    secondaryContainer = secondaryContainer,
                    onSecondaryContainer = onSecondaryContainer,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    tertiaryContainer = tertiaryContainer,
                    onTertiaryContainer = onTertiaryContainer,
                    background = background,
                    onBackground = onBackground,
                    surface = surface,
                    onSurface = onSurface,
                    surfaceVariant = surfaceVariant,
                    onSurfaceVariant = onSurfaceVariant,
                    surfaceTint = elevationOverlay,
                    inverseSurface = surfaceInverse,
                    inverseOnSurface = onSurfaceInverse,
                    outline = outline,
                    error = error,
                    onError = onError,
                    errorContainer = errorContainer,
                    onErrorContainer = onErrorContainer
                )
            }
        } else null

        /**
         * Next we'll create a typography instance, using the Material Theme text appearances
         * for TextStyles.
         *
         * We create a normal 'empty' instance first to start from the defaults, then merge in our
         * created text styles from the Android theme.
         */

        val typography = if (readTypography) {
            val defaultFontFamily = if (setDefaultFontFamily) {
                val defaultFontFamilyWithWeight: FontFamilyWithWeight? = ta.getFontFamilyOrNull(
                    R.styleable.ComposeThemeAdapterTheme_fontFamily
                ) ?: ta.getFontFamilyOrNull(R.styleable.ComposeThemeAdapterTheme_android_fontFamily)
                defaultFontFamilyWithWeight?.fontFamily
            } else {
                null
            }
            Typography(
                displayLarge = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceDisplayLarge),
                    setTextColors,
                    defaultFontFamily
                ),
                displayMedium = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceDisplayMedium),
                    setTextColors,
                    defaultFontFamily
                ),
                displaySmall = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceDisplaySmall),
                    setTextColors,
                    defaultFontFamily
                ),
                headlineLarge = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadlineLarge),
                    setTextColors,
                    defaultFontFamily
                ),
                headlineMedium = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadlineMedium),
                    setTextColors,
                    defaultFontFamily
                ),
                headlineSmall = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceHeadlineSmall),
                    setTextColors,
                    defaultFontFamily
                ),
                titleLarge = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceTitleLarge),
                    setTextColors,
                    defaultFontFamily
                ),
                titleMedium = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceTitleMedium),
                    setTextColors,
                    defaultFontFamily
                ),
                titleSmall = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceTitleSmall),
                    setTextColors,
                    defaultFontFamily
                ),
                bodyLarge = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceBodyLarge),
                    setTextColors,
                    defaultFontFamily
                ),
                bodyMedium = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceBodyMedium),
                    setTextColors,
                    defaultFontFamily
                ),
                bodySmall = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceBodySmall),
                    setTextColors,
                    defaultFontFamily
                ),
                labelLarge = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceLabelLarge),
                    setTextColors,
                    defaultFontFamily
                ),
                labelMedium = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceLabelMedium),
                    setTextColors,
                    defaultFontFamily
                ),
                labelSmall = textStyleFromTextAppearance(
                    context,
                    density,
                    ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_textAppearanceLabelSmall),
                    setTextColors,
                    defaultFontFamily
                ),
            )
        } else null

        /**
         * Now read the shape appearances, taking into account the layout direction.
         */
        val shapes = if (readShapes) {
            Shapes(
                extraSmall = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceCornerExtraSmall),
                    fallbackShape = emptyShapes.extraSmall,
                    layoutDirection = layoutDirection
                ),
                small = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceCornerSmall),
                    fallbackShape = emptyShapes.small,
                    layoutDirection = layoutDirection
                ),
                medium = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceCornerMedium),
                    fallbackShape = emptyShapes.medium,
                    layoutDirection = layoutDirection
                ),
                large = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceCornerLarge),
                    fallbackShape = emptyShapes.large,
                    layoutDirection = layoutDirection
                ),
                extraLarge = parseShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceCornerExtraLarge),
                    fallbackShape = emptyShapes.extraLarge,
                    layoutDirection = layoutDirection
                )
            )
        } else null

        Theme3Parameters(colorScheme, typography, shapes)
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
