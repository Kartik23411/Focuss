package com.kartik.focuss.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimeSelectionCard(
    modifier: Modifier = Modifier,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // heading
        Text(
            text = "Select Time",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        )
        // divider
        HorizontalDivider(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            thickness = 1.5.dp
        )

        Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TimeSlider(
                sliderValue = sliderValue,
                onSliderValueChange = onSliderValueChange,
                Modifier.fillMaxWidth(.8f)
            )
            Text(
                text = sliderValue.toTime() + " minutes",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(8.dp)
            )
        }

    }
}