package com.barutdev.tullab.ui.screens.homework.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.barutdev.tullab.R
import com.barutdev.tullab.ui.screens.homework.HomeworkFilter
import com.barutdev.tullab.util.tullabStringResource

@Composable
fun HomeworkFilterChips(
    selectedFilter: HomeworkFilter,
    onFilterSelected: (HomeworkFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        HomeworkFilter.ALL to R.string.homework_filter_all,
        HomeworkFilter.PENDING to R.string.homework_filter_pending,
        HomeworkFilter.COMPLETED to R.string.homework_filter_completed
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { (filter, labelResId) ->
            val isSelected = filter == selectedFilter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = tullabStringResource(id = labelResId)) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null
                        )
                    }
                } else null
            )
        }
    }
}
