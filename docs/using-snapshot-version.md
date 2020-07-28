# Using a Snapshot Version of the Library

If you would like to depend on the cutting edge version of the MDC-Android Compose Theme Adapter
library, you can use the
[snapshot versions][packages]
that are published to
[GitHub Packages](https://help.github.com/en/packages/publishing-and-managing-packages/about-github-packages). These are updated on every commit to `develop`.

To do so, you need to
[create a GitHub access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line#creating-a-token),
and add the following to your `build.gradle` Maven repositories:

```groovy
maven {
    url = uri("https://maven.pkg.github.com/material-components/material-components-android-compose-theme-adapter")
    credentials {
        username = <github_username>
        password = <github_access_token>
    }
}
```

Then you can use a snapshot version by adding a
`com.google.android.material:compose-theme-adapter:<version>-SNAPSHOT` dependency as per
usual (see latest release
[here][versions]).
This will fetch the latest snapshot version, which your Gradle build won't
cache. If you build after a new version has been published, that version will be
used.

See the offical doc on
[Configuring Gradle for use with GitHub Packages](https://help.github.com/en/github/managing-packages-with-github-packages/configuring-gradle-for-use-with-github-packages)
for additional information.

If you prefer to depend on a specific snapshot version, you can add
`com.google.android.material:compose-theme-adapter:<version>-<uniqueversion>`, where
`<uniqueversion>` is a combination of the date, a timestamp, and a counter (see
all versions
[here](https://github.com/material-components/material-components-android/packages/81484/versions)).

 [packages]: https://github.com/material-components/material-components-android-compose-theme-adapter/packages/328123
 [versions]: https://github.com/material-components/material-components-android-compose-theme-adapter/packages/328123/versions
