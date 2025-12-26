package com.tajir.sarf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import com.tajir.sarf.data.CurrencyGuide
import androidx.compose.ui.res.stringResource
import com.tajir.sarf.R
import androidx.compose.ui.platform.LocalContext

@Composable
fun CurrencyValueCard(
    card: CurrencyGuide.ValueCard,
    homeCurrency: String?,
    rate: Double?,
    rateUnavailable: Boolean,
    showConverted: Boolean,
    onConvertClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large
    val image = rememberAssetImage(card.denomination.assetPath, trimTransparentPadding = false)
    val localValue = card.denomination.valueCents / 100.0
    val context = LocalContext.current

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (image != null) {
                if (card.denomination.isCoin) {
                    Image(
                        bitmap = image,
                        contentDescription = card.denomination.displayLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(92.dp)
                            .aspectRatio(1f)
                    )
                } else {
                    Image(
                        bitmap = image,
                        contentDescription = card.denomination.displayLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(78.dp)
                            .aspectRatio(2.2f)
                    )
                }
            }

            Text(
                text = card.denomination.displayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = CurrencyGuide.localize(context, card.shortHint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Conversion row (revealed/updated by ↔ button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val conversionText = when {
                    homeCurrency == null -> stringResource(R.string.currency_select_prompt)
                    !showConverted -> stringResource(R.string.tap_to_convert_prompt)
                    rate != null -> {
                        val converted = localValue * rate
                        val formatted = String.format(java.util.Locale.US, "%.2f", converted)
                        "≈ $formatted $homeCurrency"
                    }
                    rateUnavailable -> stringResource(R.string.rate_unavailable)
                    else -> stringResource(R.string.loading)
                }

                Text(
                    text = conversionText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onConvertClick, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.SwapHoriz,
                        contentDescription = stringResource(R.string.convert),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                    )
                }
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}


