package com.tajir.sarf.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.tajir.sarf.R
import com.tajir.sarf.data.Denominations
import com.tajir.sarf.utils.toLatinDigits

@Composable
fun MoneyAmountText(
    cents: Int,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    currencySmall: Boolean = true,
    numberFontFamily: FontFamily = FontFamily.Monospace,
    currencyCode: String = "MAD"
) {
    val context = LocalContext.current
    val isArabic = context.resources.configuration.locales[0].language == "ar"
    val number = Denominations.formatCentsCompactNumber(cents).toLatinDigits()
    val normalized = currencyCode.trim().uppercase()
    val currency = when (normalized) {
        "MAD" -> if (isArabic) stringResource(R.string.currency_dirham) else stringResource(R.string.currency_dhs)
        "EUR" -> "€"
        "USD" -> "$"
        "GBP" -> "£"
        "SAR" -> "ر.س"
        "JPY" -> "¥"
        "CNY" -> "¥"
        "INR" -> "₹"
        "AUD" -> "A$"
        "CAD" -> "C$"
        "NZD" -> "NZ$"
        "CHF" -> "CHF"
        "SEK" -> "kr"
        "NOK" -> "kr"
        "DKK" -> "kr"
        "PLN" -> "zł"
        "CZK" -> "Kč"
        "HUF" -> "Ft"
        "RON" -> "lei"
        "BGN" -> "лв"
        "AED" -> "د.إ"
        "ILS" -> "₪"
        "JOD" -> "د.ا"
        "EGP" -> "ج.م"
        "QAR" -> "ر.ق"
        "KWD" -> "د.ك"
        "BHD" -> "د.ب"
        "OMR" -> "ر.ع."
        "RUB" -> "₽"
        "TRY" -> "₺"
        "BRL" -> "R$"
        "MXN" -> "$"
        "ZAR" -> "R"
        "KRW" -> "₩"
        "SGD" -> "S$"
        "HKD" -> "HK$"
        "TWD" -> "NT$"
        "THB" -> "฿"
        "IDR" -> "Rp"
        "PHP" -> "₱"
        "VND" -> "₫"
        "MYR" -> "RM"
        "PKR" -> "₨"
        "BDT" -> "৳"
        "LKR" -> "Rs"
        "NPR" -> "Rs"
        else -> normalized
    }

    val annotated: AnnotatedString = buildAnnotatedString {
        val numberStart = length
        append(number)
        val numberEnd = length
        addStyle(SpanStyle(fontFamily = numberFontFamily), numberStart, numberEnd)
        append(" ")
        if (isArabic && currencySmall) {
            withStyle(
                SpanStyle(
                    fontSize = (if (fontSize != TextUnit.Unspecified) fontSize * 0.65f else MaterialTheme.typography.bodyMedium.fontSize),
                    fontWeight = FontWeight.Medium,
                    // Keep currency in a normal font (Arabic looks better than monospace).
                    fontFamily = FontFamily.Default
                )
            ) { append(currency) }
        } else {
            append(currency)
        }
    }

    val resolvedAlign = textAlign ?: TextAlign.Start
    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = resolvedAlign
        )
    )
}


