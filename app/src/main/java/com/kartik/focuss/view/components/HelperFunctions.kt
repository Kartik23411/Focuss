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

fun getRandomMessage(): String {
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

val list1 = listOf<String>(
    " mins gone 🤙🏻 Stop Scrolling!",
    " mins ❌ Do something productive now!",
    " mins down the drain, move your ass 🍑 now!",
    " mins wasted on Instagram, bas kar bhai!",
    " mins ho gaye, ab ruk bhi ja! 🤨",
    " mins of scrolling... ab kya Nobel Prize milega? 🏆",
)
val list2 = listOf<String>( " mins gone 😡 Karu teri Guddi laal?",
" mins ka scrollathon! Uth ja, warna phone chipak jayega! 🛑",
" mins ho gaye.. ab Instagram wale tujhe salary denge kya? 😂",
" mins ho gaye, maa baap kya kahenge? 😭",
" mins chale gayi, teri izzat bhi chali jaayegi! 😤",
" mins ho gaye, ab bas reel-life se real-life me aa! 📵",
" mins scrolling.. koi medal nahi milega bhai! 🏅",
" mins ho gaye, teri battery nahi thaki? 🔋"
//Aree bhai X mins se scroll kar raha hai, tu thakta nahi kya? 😵‍💫
)
val list3 = listOf<String>(
   " mins wasted 🤬 You idiot!",
//Tabhi tum ese ho! X mins se Instagram 🤦
" mins barbaad 📉 Abey Gadhe 🤡",
 " mins se Reels, Hai koi sharam ya nahi? 😭",
" mins gone! Ab tu reel ka hero nahi, apni life ka villain hai! 😡",
" mins ho gaye, resume me “Instagram Scroller” likhega? 📄",
" mins barbaad! Tu khud ek meme ban chuka hai! 🤡",
" mins of scrolling, ab dimaag bhi uninstall ho gaya hoga! 🧠❌"
)
val list4 = listOf<String>(
    "Bas kar saale! Itna time padhai ki hoti to IIT me hota! 🎓",
    "Shabash, bilkul ese he sapne poore honge!",
    "Shabash beta, 1 ghanta aur reels dekh le 🤡👏",
    "Reels ke chode, sharam kar and get a life! 📲",
    "Pookie Beta 💘 kuch kaam karlo!"
)

fun getMessage(usedMinutes: Int): String {
    when (usedMinutes) {
        in 1..15  -> return list1.random()
        in 15..30 -> return list2.random()
        in 30..45 -> return list3.random()
        in 45..60 -> return list4.random()
        else      -> return list4.random()
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