package com.tajir.sarf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.tajir.sarf.data.CurrencyGuide

/**
 * Clean guide grid card: shows ONLY the money image (tourist-friendly).
 */
@Composable
fun CurrencyMoneyGridCard(
    card: CurrencyGuide.ValueCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val image = rememberAssetImage(card.denomination.assetPath, trimTransparentPadding = false)
    // User preference: money should be shown as-is (no border/line/container).
    val pad = if (card.denomination.isCoin) 12.dp else 8.dp

    BoxWithConstraints(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(pad),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = card.denomination.displayLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


