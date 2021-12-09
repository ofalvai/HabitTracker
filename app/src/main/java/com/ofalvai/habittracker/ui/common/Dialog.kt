/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui.common

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ofalvai.habittracker.R

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    description: String,
    confirmText: String,
    dismissText: String = stringResource(R.string.common_cancel),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.error)
                ) {
                    Text(text = confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(text = dismissText)
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6
                )
            },
            text = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.body1
                )
            }
        )
    }
}