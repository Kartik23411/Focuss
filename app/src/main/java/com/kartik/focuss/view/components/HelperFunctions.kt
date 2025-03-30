package com.kartik.focuss.view.components

import android.util.Log
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
val list1 = listOf<String>(
    " mins gone 🤙🏻 Stop Scrolling!",
    " mins ❌ Do something productive now!",
    " mins to gye Pookie 🎀 Ab kuch kaam karle",
    " mins Khatam, Tata, Goodbye 🤡",
    " mins ho gaye, ab ruk bhi ja! 🤨",
    " mins ho gaye, teri battery nahi thaki? 🔋",
    " mins of scrolling? Ab Nobel Prize bheju?🏆",
)
val list2 = listOf<String>( " mins gone 😡 Karu teri Guddi laal?",
    " mins barbad! Uth varna phone chipak jayega",
    " mins down the drain, move your ass 🍑 now",
    " mins wasted on Instagram, ab bas kar yar!",
    " mins 👎 ab Instagram wale nahi denge! 😭",
    " mins to ud gye, ab 1 ghanta aur uda le 😡",
    " mins wasted, Ab Maa-Baap kya kahenge? 😭",
    " mins to gye, ab teri izzat bhi jaayegi🤙🏻",
    " mins gone, ab to real-life me aaja😭",
    " mins scrolling.. koi medal nahi milega🏅",
    " mins se scroll karra, tu thakta nahi kya?",
)
val list3 = listOf<String>(
    " mins wasted 🤬 You idiot!",
    " mins barbaad 📉 Gadhe kahi ke! 🤡",
    " mins se Reels, hai koi sharam ya nahi? 😭",
    " mins ❌ resume me Instagram scroller likhle",
    " mins wasted! Tu khud ek meme ban jayega🤡",
    " mins 👎, ab to dimaag b hang hone lagega🧠"
)
val list4 = listOf<String>(
//    "Reels ke chode, sharam kar & get a life 😭",
    " mins bardbad, kasam hai tujhe ab ruk ja😡",
    " mins ke baad bhi sharam nahi ayi? 😡",
    " mins 👎 Phone me ghus kar marunga ab 👊😡",
    " mins gone Bish, move your a*s now!"
)

fun getMessage(usedMinutes: Int): String {
    when (usedMinutes) {
        in 0 .. 15  -> return list1.random()
        in 15 .. 30 -> return list2.random()
        in 30 ..45 -> return list3.random()
        in 45..60 -> return list4.random()
        else      -> return list4.random()
    }
}

fun getVibrationPattern(extraMinutes: Int): LongArray{
    Log.d("Vibration", "$extraMinutes")
    return when {
        extraMinutes in 0..15 -> longArrayOf(0, 500, 200, 500)
        extraMinutes in 15..30 -> longArrayOf(0, 750, 200, 750,200,750)
        extraMinutes in 30..45 -> longArrayOf(0, 1000, 200, 1000, 200, 1000,200,1000)
        else -> longArrayOf(0, 1500, 700, 1500, 200, 1500, 200, 1500,200,1500,200,1500)
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