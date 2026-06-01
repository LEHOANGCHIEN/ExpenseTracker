package com.expensetracker.app.feature.category.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ICON_SET = listOf(
    // Food & Drink
    "🍔", "🍕", "🍣", "🌮", "🍜", "🍱", "🥗", "🍩", "🍦", "☕", "🍺", "🍷", "🥂", "🍰",
    // Transport
    "🚗", "🚕", "🚌", "✈️", "🚇", "🚢", "⛽", "🚲", "🛵", "🚂",
    // Shopping
    "🛒", "🛍️", "👕", "👟", "👜", "💄", "🎁", "💍",
    // Health & Fitness
    "💊", "🏥", "💉", "🏃", "🧘", "🏋️", "🦷",
    // Entertainment
    "🎬", "🎮", "🎵", "🎤", "🎭", "🎪", "🎯", "🎲",
    // Education
    "📚", "🎓", "🖊️", "💻", "📖",
    // Finance
    "💰", "💳", "📈", "🏦", "💵", "📊",
    // Home
    "🏠", "💡", "🔧", "🛋️", "🌱", "🔑",
    // People & Social
    "👶", "🐶", "🐱", "❤️", "💝", "👫",
    // Other
    "📱", "🌐", "🌍", "🌟", "🎂", "🎉", "⚡", "🔔", "📦", "🏆",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerSheet(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Choose Icon",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            ) {
                items(ICON_SET) { icon ->
                    val isSelected = icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape,
                                ) else Modifier
                            )
                            .clickable { onIconSelected(icon) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = icon, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
