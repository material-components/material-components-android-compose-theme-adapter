# MDC-Android Compose Theme Adapter

A library that enables reuse of Material themes defined in XML for theming in [Jetpack Compose][compose].

The basis of theming in Jetpack Compose is the [`MaterialTheme`][materialtheme] composable, where you provide [`ColorPalette`](https://developer.android.com/reference/kotlin/androidx/ui/material/ColorPalette), [`Shapes`](https://developer.android.com/reference/kotlin/androidx/ui/material/Shapes) and [`Typography`](https://developer.android.com/reference/kotlin/androidx/ui/material/Typography) instances containing your styling parameters:

``` kotlin
MaterialTheme(
    typography = type,
    colors = colors,
    shapes = shapes
) {
    // Surface, Scaffold, etc
}
```

This library allows you to re-use your existing [Material Design Components for Android](https://github.com/material-components/material-components-android) themes within Jetpack Compose, like so:


``` kotlin
MaterialThemeFromMdcTheme {
    // MaterialTheme.colors, MaterialTheme.shapes, MaterialTheme.typography
    // will now contain copies of the context theme
}
```

This is especially handy when you're migrating an existing app, a fragment (or other UI container) a piece at a time.

### Customizing the theme

The `MaterialThemeFromMdcTheme()` function will automatically read the host context's MDC theme and pass them to [`MaterialTheme`][materialtheme] on your behalf, but if you want to customize the generated values, you can do so via the `generateMaterialThemeFromMdcTheme()` function:

``` kotlin
val context = ContextAmbient.current
var (colors, type, shapes) = generateMaterialThemeFromMdcTheme(context)

// Modify colors, type or shapes as required. Then pass them
// through to MaterialTheme...

MaterialTheme(
    typography = type,
    colors = colors,
    shapes = shapes
) {
    // rest of layout
}
```

</details>

## Limitations

There are some known limitations with the implementation at the moment:

* This relies on your Activity/Context theme extending one of the `Theme.MaterialComponents` themes.
* Text colors are not read from any text appearances by default. You can enable it via the `setTextColors` function parameter.
* `android:fontVariationSettings` is currently not supported, as variable fonts are not supported in Compose yet.
* MDC `ShapeAppearances` allow setting of corner families (cut, rounded) per corner, whereas Compose's [Shapes][shapes] allows a single corner family for the entire shape. Therefore only the `app:cornerFamily` attribute is read, others (`app:cornerFamilyTopLeft`, etc) are ignored.

---

## Contributions

Please contribute! We will gladly review any pull requests.
Make sure to read the [Contributing](CONTRIBUTING.md) page first though.

## License

```
Copyright 2020 The Android Open Source Project
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

 [compose]: https://developer.android.com/jetpack/compose
 [mdc]: https://material.io/develop/android/
 [materialtheme]: https://developer.android.com/reference/kotlin/androidx/ui/material/MaterialTheme
 [shapes]: https://developer.android.com/reference/kotlin/androidx/ui/material/Shapes