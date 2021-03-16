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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.res.FontResourcesParserCompat
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.use
import kotlin.concurrent.getOrSet

internal fun textStyleFromTextAppearance(
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
        val fontFamily: FontFamilyWithWeight? = a.getFontFamilyOrNull(
            R.styleable.ComposeThemeAdapterTextAppearance_fontFamily
        ) ?: a.getFontFamilyOrNull(R.styleable.ComposeThemeAdapterTextAppearance_android_fontFamily)

        TextStyle(
            color = when {
                setTextColors -> {
                    a.getComposeColor(R.styleable.ComposeThemeAdapterTextAppearance_android_textColor)
                }
                else -> Color.Unspecified
            },
            fontSize = a.getTextUnit(R.styleable.ComposeThemeAdapterTextAppearance_android_textSize, density),
            lineHeight = run {
                a.getTextUnitOrNull(R.styleable.ComposeThemeAdapterTextAppearance_lineHeight, density)
                    ?: a.getTextUnitOrNull(R.styleable.ComposeThemeAdapterTextAppearance_android_lineHeight, density)
                    ?: TextUnit.Unspecified
            },
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
                if (shadowColor != Color.Unspecified) {
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
                // Normally we'd use TextUnit.Unspecified,
                // but this can cause a crash due to mismatched Sp and Em TextUnits
                // https://issuetracker.google.com/issues/182881244
                else -> 0.em
            }
        )
    }
}

internal fun parseShapeAppearance(
    context: Context,
    @StyleRes id: Int,
    fallbackShape: CornerBasedShape,
    layoutDirection: LayoutDirection
): CornerBasedShape {
    return context.obtainStyledAttributes(id, R.styleable.ComposeThemeAdapterShapeAppearance).use { a ->
        val defaultCornerSize = a.getCornerSizeOrNull(
            R.styleable.ComposeThemeAdapterShapeAppearance_cornerSize
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
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val cornerSizeTS = if (isRtl) cornerSizeTR else cornerSizeTL
        val cornerSizeTE = if (isRtl) cornerSizeTL else cornerSizeTR
        val cornerSizeBS = if (isRtl) cornerSizeBR else cornerSizeBL
        val cornerSizeBE = if (isRtl) cornerSizeBL else cornerSizeBR

        /**
         * We do not support the individual `cornerFamilyTopLeft`, etc, since Compose only supports
         * one corner type per shape. Therefore we only read the `cornerFamily` attribute.
         */
        when (a.getInt(R.styleable.ComposeThemeAdapterShapeAppearance_cornerFamily, 0)) {
            0 -> {
                RoundedCornerShape(
                    topStart = cornerSizeTS ?: defaultCornerSize ?: fallbackShape.topStart,
                    topEnd = cornerSizeTE ?: defaultCornerSize ?: fallbackShape.topEnd,
                    bottomEnd = cornerSizeBE ?: defaultCornerSize ?: fallbackShape.bottomEnd,
                    bottomStart = cornerSizeBS ?: defaultCornerSize ?: fallbackShape.bottomStart
                )
            }
            1 -> {
                CutCornerShape(
                    topStart = cornerSizeTS ?: defaultCornerSize ?: fallbackShape.topStart,
                    topEnd = cornerSizeTE ?: defaultCornerSize ?: fallbackShape.topEnd,
                    bottomEnd = cornerSizeBE ?: defaultCornerSize ?: fallbackShape.bottomEnd,
                    bottomStart = cornerSizeBS ?: defaultCornerSize ?: fallbackShape.bottomStart
                )
            }
            else -> throw IllegalArgumentException("Unknown cornerFamily set in ShapeAppearance")
        }
    }
}

private val tempTypedValue = ThreadLocal<TypedValue>()

internal fun TypedArray.getComposeColor(
    index: Int,
    fallbackColor: Color = Color.Unspecified
): Color = if (hasValue(index)) Color(getColorOrThrow(index)) else fallbackColor

/**
 * Returns the given index as a [FontFamily] and [FontWeight],
 * or `null` if the value can not be coerced to a [FontFamily].
 *
 * @param index index of attribute to retrieve.
 */
internal fun TypedArray.getFontFamilyOrNull(index: Int): FontFamilyWithWeight? {
    val tv = tempTypedValue.getOrSet(::TypedValue)
    if (getValue(index, tv) && tv.type == TypedValue.TYPE_STRING) {
        return when (tv.string) {
            "sans-serif" -> FontFamilyWithWeight(FontFamily.SansSerif)
            "sans-serif-thin" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Thin)
            "sans-serif-light" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Light)
            "sans-serif-medium" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Medium)
            "sans-serif-black" -> FontFamilyWithWeight(FontFamily.SansSerif, FontWeight.Black)
            "serif" -> FontFamilyWithWeight(FontFamily.Serif)
            "cursive" -> FontFamilyWithWeight(FontFamily.Cursive)
            "monospace" -> FontFamilyWithWeight(FontFamily.Monospace)
            // TODO: Compose does not expose a FontFamily for all strings yet
            else -> {
                // If there's a resource ID and the string starts with res/font,
                // it's probably a @font resource
                if (tv.resourceId != 0 && tv.string.startsWith("res/font")) {
                    // If we're running on API 23+ and the resource is an XML, we can parse
                    // the fonts into a full FontFamily.
                    if (Build.VERSION.SDK_INT >= 23 && tv.string.endsWith(".xml")) {
                        resources.parseXmlFontFamily(tv.resourceId)?.let(::FontFamilyWithWeight)
                    } else {
                        // Otherwise we just load it as a single font
                        FontFamilyWithWeight(Font(tv.resourceId).toFontFamily())
                    }
                } else null
            }
        }
    }
    return null
}

@SuppressLint("RestrictedApi") // FontResourcesParserCompat.*
@RequiresApi(23) // XML font families with >1 fonts are only supported on API 23+
private fun Resources.parseXmlFontFamily(resourceId: Int): FontFamily? {
    val parser = getXml(resourceId)

    // Can't use {} since XmlResourceParser is AutoCloseable, not Closeable
    @Suppress("ConvertTryFinallyToUseCall")
    try {
        val result = FontResourcesParserCompat.parse(parser, this)
        if (result is FontResourcesParserCompat.FontFamilyFilesResourceEntry) {
            val fonts = result.entries.map { font ->
                Font(
                    resId = font.resourceId,
                    weight = fontWeightOf(font.weight),
                    style = if (font.isItalic) FontStyle.Italic else FontStyle.Normal
                )
            }
            return FontFamily(fonts)
        }
    } finally {
        parser.close()
    }
    return null
}

private fun fontWeightOf(weight: Int): FontWeight = when (weight) {
    in 0..149 -> FontWeight.W100
    in 150..249 -> FontWeight.W200
    in 250..349 -> FontWeight.W300
    in 350..449 -> FontWeight.W400
    in 450..549 -> FontWeight.W500
    in 550..649 -> FontWeight.W600
    in 650..749 -> FontWeight.W700
    in 750..849 -> FontWeight.W800
    in 850..999 -> FontWeight.W900
    // Else, we use the 'normal' weight
    else -> FontWeight.W400
}

internal data class FontFamilyWithWeight(
    val fontFamily: FontFamily,
    val weight: FontWeight = FontWeight.Normal
)

/**
 * Returns the given index as a [TextUnit], or [fallback] if the value can not be coerced to
 * a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 * @param fallback Value to return if the attribute is not defined or cannot be coerced to
 * a [TextUnit].
 */
internal fun TypedArray.getTextUnit(
    index: Int,
    density: Density,
    fallback: TextUnit = TextUnit.Unspecified
): TextUnit = getTextUnitOrNull(index, density) ?: fallback

/**
 * Returns the given index as a [TextUnit], or `null` if the value can not be coerced to
 * a [TextUnit].
 *
 * @param index index of attribute to retrieve.
 * @param density the current display density.
 */
internal fun TypedArray.getTextUnitOrNull(
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
 * Returns the given index as a [CornerSize], or `null` if the value can not be coerced
 * to a [CornerSize].
 *
 * @param index index of attribute to retrieve.
 */
internal fun TypedArray.getCornerSizeOrNull(index: Int): CornerSize? {
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
 * A workaround since [TypedValue.getComplexUnit] is API 22+
 */
private inline val TypedValue.complexUnitCompat
    get() = when {
        Build.VERSION.SDK_INT > 22 -> complexUnit
        else -> TypedValue.COMPLEX_UNIT_MASK and (data shr TypedValue.COMPLEX_UNIT_SHIFT)
    }
