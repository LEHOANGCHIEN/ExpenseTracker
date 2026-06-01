package com.expensetracker.app.feature.wallet.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expensetracker.app.core.util.CurrencyFormatter
import com.expensetracker.app.feature.wallet.WalletItem

@Composable
fun WalletCard(
    item: WalletItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseColor = runCatching {
        Color(android.graphics.Color.parseColor(item.wallet.color))
    }.getOrElse { MaterialTheme.colorScheme.primary }

    val darkColor = Color(
        red = (baseColor.red * 0.65f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.65f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.65f).coerceIn(0f, 1f),
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = listOf(baseColor, darkColor)),
                )
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = item.wallet.icon.ifBlank { "💵" },
                        fontSize = 28.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.wallet.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                }
                Text(
                    text = item.wallet.currency,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = CurrencyFormatter.format(item.balance, item.wallet.currency),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )
            Text(
                text = "Current balance",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}
