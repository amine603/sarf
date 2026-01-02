package com.cash.guide.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Museum
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

private data class ValueIconSpec(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)

/**
 * USD reference buckets -> real-life icons.
 *
 * Rules:
 * - Same USD value => same bucket => same icon set (global, country-agnostic).
 * - No per-country prices; icons are purely educational.
 */
private fun iconsForUsd(usd: Double): List<ValueIconSpec> {
    val v = usd.coerceAtLeast(0.0)
    return when {
        v in 0.5..1.5 -> listOf(
            ValueIconSpec(Icons.Outlined.LocalDrink, "Water", "Water / drink"),
            ValueIconSpec(Icons.Outlined.Star, "Candy", "Small treat"),
            ValueIconSpec(Icons.Outlined.LocalCafe, "Juice", "Small juice")
        )
        v in 2.0..4.0 -> listOf(
            ValueIconSpec(Icons.Outlined.LocalCafe, "Coffee", "Coffee"),
            ValueIconSpec(Icons.Outlined.Star, "Snack", "Snack"),
            ValueIconSpec(Icons.Outlined.LocalDrink, "Tea", "Tea / drink")
        )
        v in 5.0..9.0 -> listOf(
            ValueIconSpec(Icons.Outlined.Fastfood, "Food", "Sandwich / fast food"),
            ValueIconSpec(Icons.Outlined.DirectionsBus, "Bus", "Bus ticket"),
            ValueIconSpec(Icons.Outlined.LocalTaxi, "Taxi", "Very short taxi")
        )
        v in 10.0..19.0 -> listOf(
            ValueIconSpec(Icons.Outlined.Restaurant, "Meal", "Cheap restaurant meal"),
            ValueIconSpec(Icons.Outlined.LocalTaxi, "Taxi", "Short taxi"),
            ValueIconSpec(Icons.Outlined.Museum, "Museum", "Museum ticket"),
            ValueIconSpec(Icons.Outlined.SimCard, "Data", "Small data pack")
        )
        v in 20.0..39.0 -> listOf(
            ValueIconSpec(Icons.Outlined.Restaurant, "Meal", "Mid restaurant meal"),
            ValueIconSpec(Icons.Outlined.Train, "Train", "Train short trip"),
            ValueIconSpec(Icons.Outlined.ShoppingCart, "Essentials", "Essentials / shopping")
        )
        v in 40.0..79.0 -> listOf(
            ValueIconSpec(Icons.Outlined.Hotel, "Hotel", "Budget hotel night"),
            ValueIconSpec(Icons.Outlined.Museum, "Tour", "City tour / ticket"),
            ValueIconSpec(Icons.Outlined.ShoppingCart, "Shopping", "Shopping items")
        )
        else -> listOf(
            ValueIconSpec(Icons.Outlined.Hotel, "Hotel", "Mid/high hotel"),
            ValueIconSpec(Icons.Outlined.Flight, "Flight", "Flight / big ticket"),
            ValueIconSpec(Icons.Outlined.Train, "Ticket", "Train ticket"),
            ValueIconSpec(Icons.Outlined.Star, "Premium", "Premium experience")
        )
    }
}

@Composable
fun ValueUnderstandingIconsRow(
    usdValue: Double,
    iconSizeDp: Int = 44,
    modifier: Modifier = Modifier
) {
    // Keep the UI clean: max 2 rows, short labels.
    val icons = iconsForUsd(usdValue).take(6)
    val rows: List<List<ValueIconSpec>> = icons.chunked(3).take(2)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { spec ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = spec.icon,
                            contentDescription = spec.contentDescription,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(iconSizeDp.dp)
                        )
                        Text(
                            text = spec.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Text(
            text = androidx.compose.ui.res.stringResource(com.cash.guide.R.string.examples_are_approximate),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
            textAlign = TextAlign.Center
        )
    }
}


