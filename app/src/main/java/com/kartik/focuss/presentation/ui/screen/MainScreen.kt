package com.kartik.focuss.presentation.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices.PIXEL_7A
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kartik.focuss.presentation.ui.components.AppSelectionCard
import com.kartik.focuss.presentation.ui.components.TimeSelectionCard
import com.kartik.focuss.ui.theme.FocussTheme

@Composable
fun MainScreen(
    onFocusClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val prefs = context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE)
        val thresholdMinutes = prefs.getInt("threshold_time", 5)
        val isEnable = prefs.getBoolean("isEnable", false)

        var isIgChecked by remember { mutableStateOf(true) }
        var isFocus by remember { mutableStateOf(isEnable) }
        var timerValue by remember { mutableFloatStateOf(thresholdMinutes.toFloat()) }

        LaunchedEffect(isFocus) {
            if (isFocus) {
                context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE).edit().apply {
                    putInt("threshold_time", timerValue.toInt())
                    apply()
                }
                context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE).edit().apply {
                    putBoolean("isEnable", true)
                    apply()
                }
                Log.d("MainScreen", "Saved threshold_time: ${timerValue.toInt()} min")
                onFocusClick()
            }
            else {
                context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE).edit().apply {
                    putBoolean("isEnable", false)
                    apply()
                }
                onStopClick()
            }
        }

        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // App Selection Card
        AppSelectionCard(
            modifier = Modifier
                    .fillMaxWidth(.85f)
                    .padding(vertical = 8.dp),
            isIgChecked,
            onCheckedChange = { isIgChecked = it },
        )

        // Spacer
        Spacer(Modifier.height(32.dp))

        // Time Selection Card
        TimeSelectionCard(
            modifier = Modifier
                    .fillMaxWidth(.85f)
                    .padding(vertical = 8.dp),
            sliderValue = timerValue,
            onSliderValueChange = { timerValue = it }
        )

//        ActionButtonRow(
//            onStopClick = { onStopClick() },
//            onFocusClick = {
//                context.getSharedPreferences("focuss_prefs", Context.MODE_PRIVATE).edit().apply {
//                    putInt("threshold_time", timerValue.toInt())
//                    apply()
//                }
//                Log.d("MainScreen", "Saved threshold_time: ${timerValue.toInt()} min")
//                onFocusClick()
//            },
//            isIgChecked = isIgChecked,
//            modifier = Modifier
//                    .weight(.5f)
//                    .padding(vertical = 24.dp)
//                    .fillMaxWidth(.85f),
//        )

        Row(
            modifier = Modifier.fillMaxWidth(.6f),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stop",
                style = MaterialTheme.typography.labelLarge
            )

            Switch(
                modifier = Modifier
                        .padding(vertical = 24.dp),
                checked = isFocus,
                onCheckedChange = {
                    isFocus = it
                },
                thumbContent = if (isFocus) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null
                        )
                    }
                }
                else {
                    null
                }
            )
            Text(
                text = "Start",
                style = MaterialTheme.typography.labelLarge
            )
        }

    }
}

@Preview(showBackground = true, device = PIXEL_7A)
@Composable
private fun MainScreenPreview() {
    FocussTheme {
        MainScreen({}, {}, modifier = Modifier.fillMaxSize())
    }
}