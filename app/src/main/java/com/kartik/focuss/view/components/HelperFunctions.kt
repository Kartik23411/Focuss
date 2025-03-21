package com.kartik.focuss.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun Float.toTime(): String {
    return if ((this % 5).toInt() == 0)
        this.toInt().toString()
    else
        (this + 1f).toInt().toString()
}

fun getRandomMessage():String{
    val messagesList = listOf<String>(
//        "You've spent X mins on Instagram, move your ass now! 🚀📵",
        "Bas kar bhai, do something productive now! ⏳💪",
    "Reels ke chode, sharam kar and get a life 😭📲",
    "Shabash beta, 1 ghanta aur reels dekh le 🤡👏",
    "Pookie, kuch kaam karle thoda! 😘📚",
    "Naa maane? Karu teri guddi laal? 😡👋🏻"

    )

    return messagesList.random()
}

val list1 = listOf<String>("Test 1.1", "Test 1.2", "Test 1.3", "Test 1.4")
val list2 = listOf<String>("Test 2.1", "Test 2.2", "Test 2.3", "Test 2.4")
val list3 = listOf<String>("Test 3.1", "Test 3.2", "Test 3.3", "Test 3.4")
val list4 = listOf<String>("Test 4.1", "Test 4.2", "Test 4.3", "Test 4.4")

fun getMessage(usedMinutes: Int): String{
    when(usedMinutes){
        in 1..5 -> return list1.random()
        in 5..15 -> return list2.random()
        in 15..30 -> return list3.random()
        else    -> return list4.random()
    }
}

@Composable
fun TimeSlider(
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = sliderValue,
            onValueChange = onSliderValueChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 4,
            valueRange = 5f..30f
        )
    }
}


@Composable
fun AppAndButtonRow(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    appName: String,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.5.dp)

    Row(
        modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}