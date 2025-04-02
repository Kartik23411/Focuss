package com.kartik.focuss.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kartik.focuss.ui.theme.FocussTheme
import kotlinx.coroutines.delay

@Composable
fun StartScreen(
    navigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(Unit) {
        delay(2000)
        navigateToNext()
    }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ),
            lineHeight = 40.sp
        )
        Text(
            text = "DoomStop helps you reclaim your time and boost productivity by keeping your " +
                    "Scrolling" +
                    " habits in check",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                    .fillMaxWidth(.8f)
                    .padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StartScreeenPreview() {
    FocussTheme {
        StartScreen({}, Modifier.fillMaxSize())
    }
}