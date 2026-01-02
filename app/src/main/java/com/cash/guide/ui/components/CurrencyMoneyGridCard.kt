package com.cash.guide.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.data.CurrencyGuide
import com.cash.guide.utils.CurrencyFormatter

/**
 * Clean guide grid card: shows the money image with localized currency value below.
 * Never displays decimals - uses subunits (cents, pence, halalas) instead.
 */
@Composable
fun CurrencyMoneyGridCard(
    card: CurrencyGuide.ValueCard,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val image = rememberAssetImage(card.denomination.assetPath, trimTransparentPadding = false)
    // User preference: money should be shown as-is (no border/line/container).
    val pad = if (card.denomination.isCoin) 10.dp else 6.dp
    
    // Format the value using localized currency formatter
    val formattedValue = CurrencyFormatter.formatDenomination(
        amountInMinorUnits = card.denomination.valueCents,
        currencyCode = currencyCode
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(pad),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = formattedValue,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        // Show localized currency value (e.g., "25 cents", "5 dollars", "50p", "1 euro")
        Text(
            text = formattedValue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


