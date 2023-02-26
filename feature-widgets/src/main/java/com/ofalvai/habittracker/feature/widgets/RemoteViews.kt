/*
 * Copyright 2023 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.feature.widgets

import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.layout.Box
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize

@Composable
fun AppWidgetRoot(
    modifier: GlanceModifier = GlanceModifier,
    content: @Composable () -> Unit
) {
    GlanceTheme {
        Box(
            GlanceModifier
                .fillMaxSize()
                .then(modifier)
        ) {
            AndroidRemoteViews(
                remoteViews = RemoteViews(LocalContext.current.packageName, R.layout.app_widget_root),
                containerViewId = R.id.app_widget_background,
                content = {
                    // Emitting the content composable here doesn't work for some reason, so we
                    // just stretch this view and draw the content on top of it
                    Spacer(GlanceModifier.fillMaxSize())
                })

            content()
        }
    }
}