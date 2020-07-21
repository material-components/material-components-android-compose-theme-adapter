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
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import androidx.annotation.StyleRes
import androidx.compose.Composable
import androidx.compose.remember
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.ui.core.ContextAmbient
import androidx.ui.foundation.shape.corner.CornerBasedShape
import androidx.ui.foundation.shape.corner.CornerSize
import androidx.ui.foundation.shape.corner.CutCornerShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.Shadow
import androidx.ui.material.ColorPalette
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Shapes
import androidx.ui.material.Typography
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.text.font.asFontFamily
import androidx.ui.text.font.font
import androidx.ui.unit.Density
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.sp
import java.lang.reflect.Method
import kotlin.concurrent.getOrSet

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

    val (colors, type, shapes) = remember(key) {
        generateMaterialThemeFromMdcTheme(
            context = context,
            readColors = readColors,
            readTypography = readTypography,
            readShapes = readShapes,
            setTextColors = setTextColors
        )
    }

    MaterialTheme(
        colors = colors ?: MaterialTheme.colors,
        typography = type ?: MaterialTheme.typography,
        shapes = shapes ?: MaterialTheme.shapes,
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
                small = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceSmallComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                medium = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceMediumComponent),
                    fallbackSize = CornerSize(4.dp)
                ),
                large = readShapeAppearance(
                    context = context,
                    id = ta.getResourceIdOrThrow(R.styleable.ComposeThemeAdapterTheme_shapeAppearanceLargeComponent),
                    fallbackSize = CornerSize(0.dp)
                )
            )
        } else null

        ThemeParameters(colors, typography, shapes)
    }
}

private fun textStyleFromTextAppearance(
    context: Context,
    density: Density,
    @StyleRes id: Int,
    setTextColors: Boolean
): TextStyle {
    return context.obtainStyledAttributes(id, R.styleable.ComposeThemeAdapterTextAppearance).use { a ->
        val textStyle = a.getInt(R.styleable.ComposeThemeAdapterTextAppearance_android_textStyle, -1)
        val textFontWeight = a.getInt(R.styleable.ComposeThemeAdapterTextAppearance_android_textFontWeight, -1)
        val typeface = a.getInt(R.styleable.ComposeThemeAdapterTextAppearance_android_typeface, -1)

        // TODO read and expand android:fontVariationSettings.
        // Variable fonts are not supported in Compose yet

        // FYI, this only works with static font files in assets
        var fontFamily: FontFamilyWithWeight? = null
        if (a.hasValue(R.styleable.ComposeThemeAdapterTextAppearance_fontFamily)) {
            fontFamily = a.getFontFamilyOrNull(
                R.styleable.ComposeThemeAdapterTextAppearance_fontFamily
            )
        }
        if (fontFamily == null &&
            a.hasValue(R.styleable.ComposeThemeAdapterTextAppearance_android_fontFamily)
        ) {
            fontFamily = a.getFontFamilyOrNull(
                R.styleable.ComposeThemeAdapterTextAppearance_android_fontFamily
            )
        }

        TextStyle(
            color = when {
                setTextColors -> {
                    a.getComposeColor(R.styleable.ComposeThemeAdapterTextAppearance_android_textColor)
                }
                else -> Color.Unset
            },
            fontSize = a.getTextUnit(R.styleable.ComposeThemeAdapterTextAppearance_android_textSize, density),
            lineHeight = a.getTextUnit(R.styleable.ComposeThemeAdapterTextAppearance_android_lineHeight, density),
            fontFamily = when {
                fontFamily != null -> fontFamily.fontFamily
                // Values below are from frameworks/base attrs.xml
                typeface == 1 -> FontFamily.SansSerif
                typeface == 2 -> FontFamily.Serif
                typeface == 3 -> FontFamily.Monospace
                else -> null
            },
            fontStyle = when {
                (textStyle and Typeface.ITALIC) != 0 -> FontStyle.Italic
                else -> FontStyle.Normal
            },
            fontWeight = when {
                textFontWeight in 0..149 -> FontWeight.W100
                textFontWeight in 150..249 -> FontWeight.W200
                textFontWeight in 250..349 -> FontWeight.W300
                textFontWeight in 350..449 -> FontWeight.W400
                textFontWeight in 450..549 -> FontWeight.W500
                textFontWeight in 550..649 -> FontWeight.W600
                textFontWeight in 650..749 -> FontWeight.W700
                textFontWeight in 750..849 -> FontWeight.W800
                textFontWeight in 850..999 -> FontWeight.W900
                // Else, check the text style for bold
                (textStyle and Typeface.BOLD) != 0 -> FontWeight.Bold
                // Else, the font family might have an implicit weight (san-serif-light, etc)
                fontFamily != null -> fontFamily.weight
                else -> null
            },
            fontFeatureSettings = a.getString(R.styleable.ComposeThemeAdapterTextAppearance_android_fontFeatureSettings),
            shadow = run {
                val shadowColor = a.getComposeColor(R.styleable.ComposeThemeAdapterTextAppearance_android_shadowColor)
                if (shadowColor != Color.Unset) {
                    val dx = a.getFloat(R.styleable.ComposeThemeAdapterTextAppearance_android_shadowDx, 0f)
                    val dy = a.getFloat(R.styleable.ComposeThemeAdapterTextAppearance_android_shadowDy, 0f)
                    val rad = a.getFloat(R.styleable.ComposeThemeAdapterTextAppearance_android_shadowRadius, 0f)
                    Shadow(color = shadowColor, offset = Offset(dx, dy), blurRadius = rad)
                } else null
            },
            letterSpacing = when {
                a.hasValue(R.styleable.ComposeThemeAdapterTextAppearance_android_letterSpacing) -> {
                    a.getFloat(R.styleable.ComposeThemeAdapterTextAppearance_android_letterSpacing, 0f).em
                }
                else -> TextUnit.Inherit
            }
        )
    }
}

private fun readShapeAppearance(
    context: Context,
    @StyleRes id: Int,
    fallbackSize: CornerSize
): CornerBasedShape {
    return context.obtainStyledAttributes(id, R.styleable.ComposeThemeAdapterShapeAppearance).use { a ->
        val defaultCornerSize = a.getCornerSize(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSize,
            fallbackSize
        )
        val cornerSizeTL = a.getCornerSizeOrNull(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSizeTopLeft
        )
        val cornerSizeTR = a.getCornerSizeOrNull(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSizeTopRight
        )
        val cornerSizeBL = a.getCornerSizeOrNull(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSizeBottomLeft
        )
        val cornerSizeBR = a.getCornerSizeOrNull(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSizeBottomRight
        )

        /**
         * We do not support the individual `cornerFamilyTopLeft`, etc, since Compose only supports
         * one corner type per shape. Therefore we only read the `cornerFamily` attribute.
         */
        when (a.getInt(R.styleable.ComposeThemeAdapterShapeAppearance_cornerFamily, 0)) {
            0 -> {
                RoundedCornerShape(
                    topLeft = cornerSizeTL ?: defaultCornerSize,
                    topRight = cornerSizeTR ?: defaultCornerSize,
                    bottomRight = cornerSizeBR ?: defaultCornerSize,
                    bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            1 -> {
                CutCornerShape(
                    topLeft = cornerSizeTL ?: defaultCornerSize,
                    topRight = cornerSizeTR ?: defaultCornerSize,
                    bottomRight = cornerSizeBR ?: defaultCornerSize,
                    bottomLeft = cornerSizeBL ?: defaultCornerSize
                )
            }
            else -> throw IllegalArgumentException("Unknown cornerFamily set in ShapeAppearance")
        }
    }
}

private fun Typography.merge(
    h1: TextStyle = TextStyle(),
    h2: TextStyle = TextStyle(),
    h3: TextStyle = TextStyle(),
    h4: TextStyle = TextStyle(),
    h5: TextStyle = TextStyle(),
    h6: TextStyle = TextStyle(),
    subtitle1: TextStyle = TextStyle(),
    subtitle2: TextStyle = TextStyle(),
    body1: TextStyle = TextStyle(),
    body2: TextStyle = TextStyle(),
    button: TextStyle = TextStyle(),
    caption: TextStyle = TextStyle(),
    overline: TextStyle = TextStyle()
) = copy(
    h1 = h1.merge(h1),
    h2 = h2.merge(h2),
    h3 = h3.merge(h3),
    h4 = h4.merge(h4),
    h5 = h5.merge(h5),
    h6 = h6.merge(h6),
    subtitle1 = subtitle1.merge(subtitle1),
    subtitle2 = subtitle2.merge(subtitle2),
    body1 = body1.merge(body1),
    body2 = body2.merge(body2),
    button = button.merge(button),
    caption = caption.merge(caption),
    overline = overline.merge(overline)
)

private val tempTypedValue = ThreadLocal<TypedValue>()

private fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unset
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or [fallback] if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [FontFamily].
 */
private fun TypedArray.getFontFamily(index: Int, fallback: FontFamily): FontFamilyWithWeight {
    return getFontFamilyOrNull(index) ?: FontFamilyWithWeight(fallback)
}

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or `null` if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 */
private fun TypedArray.getFontFamilyOrNull(index: Int): FontFamilyWithWeight? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_STRING) {
        if (tv.resourceId != 0) {
            // If there's a resource ID, it's probably a @font resource
            return FontFamilyWithWeight(font(tv.resourceId).asFontFamily())
        }
        return when (tv.string) {
            "san-serif" -> FontFamilyWithWeight(FontFamily.SansSerif)
            "sans-serif-thin" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Thin)
            "san-serif-light" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            "sans-serif-medium" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Medium)
            "sans-serif-black" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            "serif" -> FontFamilyWithWeight(FontFamily.Serif)
            "cursive" -> FontFamilyWithWeight(FontFamily.Cursive)
            "monospace" -> FontFamilyWithWeight(FontFamily.Monospace)
            // TODO: Compose does not expose a FontFamily for "sans-serif-condensed" yet
            else -> null
        }
    }
    return null
}

private data class FontFamilyWithWeight(
    val fontFamily: FontFamily,
    val weight: FontWeight = FontWeight.Normal
)

/**
 * Returns the given index as a [TextUnit], or [fallback] if the value can not be coerced to a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [TextUnit].
 */
private fun TypedArray.getTextUnit(
    index: Int,
    density: Density,
    fallback: TextUnit = TextUnit.Inherit
): TextUnit = getTextUnitOrNull(index, density) ?: fallback

/**
 * Returns the given index as a [TextUnit], or `null` if the value can not be coerced to a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 */
private fun TypedArray.getTextUnitOrNull(
    index: Int,
    density: Density
): TextUnit? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_DIMENSION) {
        return when (tv.complexUnitCompat) {
            // For SP values, we convert the value directly to an TextUnit.Sp
            TypedValue.COMPLEX_UNIT_SP -> TypedValue.complexToFloat(tv.data).sp
            // For DIP values, we convert the value to an TextUnit.Em (roughly equivalent)
            TypedValue.COMPLEX_UNIT_DIP -> TypedValue.complexToFloat(tv.data).em
            // For another other types, we let the TypedArray flatten to a px value, and
            // we convert it to an Sp based on the current density
            else -> with(density) { getDimension(index, 0f).toSp() }
        }
    }
    return null
}

/**
 * Returns the given index as a [CornerSize], or `null` if the value can not be coerced to a [CornerSize].
 *
 * @param index index of attribute to retrieve.
 */
private fun TypedArray.getCornerSizeOrNull(index: Int): CornerSize? {
    val tv = tempTypedValue.getOrSet { TypedValue() }
    if (getValue(index, tv)) {
        return when (tv.type) {
            TypedValue.TYPE_DIMENSION -> {
                when (tv.complexUnitCompat) {
                    // For DIP and PX values, we convert the value to the equivalent
                    TypedValue.COMPLEX_UNIT_DIP -> CornerSize(TypedValue.complexToFloat(tv.data).dp)
                    TypedValue.COMPLEX_UNIT_PX -> CornerSize(TypedValue.complexToFloat(tv.data))
                    // For another other dim types, we let the TypedArray flatten to a px value
                    else -> CornerSize(getDimensionPixelSize(index, 0))
                }
            }
            TypedValue.TYPE_FRACTION -> CornerSize(tv.getFraction(1f, 1f))
            else -> null
        }
    }
    return null
}

/**
 * Returns the given index as a [CornerSize], or [fallback] if the value can not be coerced to a [CornerSize].
 *
 * @param index index of attribute to retrieve.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to an [CornerSize].
 */
private fun TypedArray.getCornerSize(index: Int, fallback: CornerSize): CornerSize {
    return getCornerSizeOrNull(index) ?: fallback
}

/**
 * A workaround since [TypedValue.getComplexUnit] is API 22+
 */
private inline val TypedValue.complexUnitCompat
    get() = when {
        Build.VERSION.SDK_INT > 22 -> complexUnit
        else -> TypedValue.COMPLEX_UNIT_MASK and (data shr TypedValue.COMPLEX_UNIT_SHIFT)
    }

/**
 * This is gross, but we need a way to check for theme equality. Theme does not implement
 * `equals()` or `hashCode()`, but it does have a hidden method called `getKey()`.
 *
 * The cost of this reflective invoke is a lot cheaper than the full theme read which currently
 * happens on every re-composition.
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
