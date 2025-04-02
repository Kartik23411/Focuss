package com.kartik.focuss.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ActionButtonRow(
    onFocusClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
    isIgChecked: Boolean
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onStopClick() },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Stop",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Button(
            onClick = { onFocusClick() },
            enabled = isIgChecked,
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.labelLarge
            )
        }

    }
}