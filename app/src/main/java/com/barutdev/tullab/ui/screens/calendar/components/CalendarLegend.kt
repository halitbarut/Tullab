package com.barutdev.tullab.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.theme.StatusBlue
import com.barutdev.tullab.ui.theme.StatusRed
import com.barutdev.tullab.ui.theme.StatusYellow
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun CalendarLegend(
    modifier: Modifier = Modifier
) {
    val legendItems = listOf(
        LegendItemData(StatusBlue, R.string.calendar_legend_scheduled),
        LegendItemData(StatusYellow, R.string.calendar_legend_completed),
        LegendItemData(StatusRed, R.string.calendar_legend_cancelled)
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            legendItems.forEach { item ->
                LegendItem(
                    color = item.color,
                    label = tullabStringResource(id = item.labelResId)
                )
            }
        }
    }
}

private data class LegendItemData(
    val color: Color,
    val labelResId: Int
)

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
