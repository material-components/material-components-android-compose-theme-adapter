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

package com.google.android.material.composethemeadapter.sample

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Recomposer
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.Spacer
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.material.Button
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.ExtendedFloatingActionButton
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.Scaffold
import androidx.ui.material.TextButton
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.res.stringResource
import androidx.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme

class BasicIntegrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentView = FrameLayout(this)
        setContentView(contentView)

        contentView.setContent(Recomposer.current()) {
            MdcTheme {
                ComponentsSample()
            }
        }
    }
}

@Composable
fun ComponentsSample() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.basic_integration))
                }
            )
        }
    ) {
        VerticalScroller {
            Column(Modifier.padding(16.dp)) {
                CircularProgressIndicator()
                VerticalSpacer()

                Button(onClick = {}) {
                    Text(text = "Button")
                }
                VerticalSpacer()

                OutlinedButton(onClick = {}) {
                    Text(text = "Outlined Button")
                }
                VerticalSpacer()

                TextButton(onClick = {}) {
                    Text(text = "Text Button")
                }
                VerticalSpacer()

                FloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Default.Favorite) }
                )
                VerticalSpacer()

                ExtendedFloatingActionButton(
                    onClick = {},
                    text = { Text(text = "Extended FAB") },
                    icon = { Icon(Icons.Default.Favorite) }
                )
                VerticalSpacer()

                Text(
                    text = "H1",
                    style = MaterialTheme.typography.h1
                )
                Text(
                    text = "Headline 2",
                    style = MaterialTheme.typography.h2
                )
                Text(
                    text = "Headline 3",
                    style = MaterialTheme.typography.h3
                )
                Text(
                    text = "Headline 4",
                    style = MaterialTheme.typography.h4
                )
                Text(
                    text = "Headline 5",
                    style = MaterialTheme.typography.h5
                )
                Text(
                    text = "Headline 6",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Subtitle 1",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Subtitle 2",
                    style = MaterialTheme.typography.subtitle2
                )
                Text(
                    text = "Body 1",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "Body 2",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Caption",
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "Overline",
                    style = MaterialTheme.typography.overline
                )
            }
        }
    }
}

@Composable
private fun VerticalSpacer() {
    Spacer(Modifier.preferredHeight(8.dp))
}
