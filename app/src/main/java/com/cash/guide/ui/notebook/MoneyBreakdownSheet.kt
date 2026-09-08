package com.cash.guide.ui.notebook

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyPiece
import com.cash.guide.domain.MoneyUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

private object BanknoteImageCache {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun decode(context: Context, path: String): ImageBitmap? {
        val existing = cache[path]
        if (existing != null) return existing
        return runCatching {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
            }
        }.getOrNull()?.also { cache[path] = it }
    }
}

@Composable
private fun rememberBanknoteImage(path: String?): ImageBitmap? {
    val context = LocalContext.current
    if (path == null) return null
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        val loaded = withContext(Dispatchers.IO) {
            BanknoteImageCache.decode(context, path)
        }
        value = loaded
    }
    return bitmapState.value
}

private fun getRialEquivalent(denominationCentimes: Long): String? = when (denominationCentimes) {
    20_000L -> "4 000 ريال"
    10_000L -> "2 000 ريال"
    5_000L -> "1 000 ريال"
    2_000L -> "400 ريال"
    1_000L -> "200 ريال"
    500L -> "100 ريال"
    200L -> "40 ريال"
    100L -> "20 ريال"
    50L -> "10 ريال"
    20L -> "4 ريال"
    10L -> "2 ريال"
    5L -> "ريال واحد"
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MoneyBreakdownSheet(
    totalCentimes: Long,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pieces = remember(totalCentimes) { MoneyMath.breakdown(totalCentimes) }
    val remainderCentimes = remember(totalCentimes, pieces) {
        val accounted = pieces.sumOf { it.denomination.valueCentimes * it.count }
        (totalCentimes - accounted).coerceAtLeast(0)
    }

    val dirhamFormatted = MoneyMath.fromCentimes(totalCentimes, MoneyUnit.DIRHAM)
    val rialFormatted = MoneyMath.fromCentimes(totalCentimes, MoneyUnit.RIAL)

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .background(HighlighterPink.copy(alpha = 0.65f), RoundedCornerShape(2.5.dp))
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.74f)
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            LocalLayoutDirection provides layoutDirection
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val spacingPx = JournalRuleSpacing.roundToPx().toFloat()
                        val lineCount = (size.height / spacingPx).toInt() + 1
                        val stroke = 0.6.dp.toPx()
                        val lineColor = JournalRule.copy(alpha = 0.32f)
                        for (i in 1..lineCount) {
                            val y = i * spacingPx
                            drawLine(
                                color = lineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = stroke
                            )
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            NotebookHighlightedBadge(
                                text = stringResource(R.string.breakdown_title),
                                highlighterColor = HighlighterPink.copy(alpha = 0.65f),
                                fontSize = 16.5.sp,
                                horizontalPadding = 12.dp,
                                verticalPadding = 3.5.dp
                            )
                            Spacer(Modifier.height(4.dp))
                            val subtitleText = stringResource(R.string.breakdown_subtitle)
                            Text(
                                text = subtitleText,
                                fontFamily = resolveJournalFont(subtitleText, isRtl),
                                fontSize = if (isRtl) 13.sp else 13.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = JournalMutedInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(JournalDockBg, RoundedCornerShape(8.dp))
                                .border(
                                    BorderStroke(0.7.dp, JournalRule.copy(alpha = 0.70f)),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = dirhamFormatted,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorOrange
                                )
                                val dirhamSuffix = stringResource(R.string.currency_dirham)
                                Text(
                                    text = dirhamSuffix,
                                    fontFamily = resolveJournalFont(dirhamSuffix, isRtl),
                                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }

                            Text(
                                text = "=",
                                fontFamily = PatrickHandFamily,
                                color = JournalMutedInk,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = rialFormatted,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 18.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorOrange
                                )
                                val rialSuffix = stringResource(R.string.currency_rial)
                                Text(
                                    text = rialSuffix,
                                    fontFamily = resolveJournalFont(rialSuffix, isRtl),
                                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(14.dp)
                                    .background(HighlighterYellow.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            )
                            val distText = stringResource(R.string.breakdown_distribution)
                            Text(
                                text = distText,
                                fontFamily = resolveJournalFont(distText, isRtl),
                                color = JournalWritingInk,
                                fontSize = if (isRtl) 14.sp else 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(0.6.dp)
                                    .background(JournalRule.copy(alpha = 0.6f))
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    pieces.forEach { piece ->
                        item(key = piece.denomination.label) {
                            BreakdownDenominationBlock(piece = piece)
                        }
                    }

                    if (remainderCentimes > 0) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(JournalDockBg, RoundedCornerShape(6.dp))
                                    .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val remText = "${stringResource(R.string.breakdown_remainder)} (< 10 centimes):"
                                Text(
                                    text = remText,
                                    fontFamily = resolveJournalFont(remText, isRtl),
                                    color = JournalInk,
                                    fontSize = if (isRtl) 13.sp else 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$remainderCentimes c",
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BreakdownDenominationBlock(piece: MoneyPiece) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
    val isBanknote = piece.denomination.valueCentimes >= 2_000L
    val bitmap = rememberBanknoteImage(piece.denomination.assetPath)
    val rialText = getRialEquivalent(piece.denomination.valueCentimes)

    val pieceTotalCentimes = piece.denomination.valueCentimes * piece.count
    val pieceTotalDh = MoneyMath.fromCentimes(pieceTotalCentimes, MoneyUnit.DIRHAM)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(JournalPaper.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .border(BorderStroke(0.55.dp, JournalRule.copy(alpha = 0.45f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isBanknote) HighlighterPink.copy(alpha = 0.25f) else HighlighterYellow.copy(alpha = 0.35f)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${piece.count} ×",
                        fontFamily = PatrickHandFamily,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBanknote) ColorCoral else ColorOrange,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }

                Text(
                    text = piece.denomination.label,
                    fontFamily = resolveJournalFont(piece.denomination.label, isRtl),
                    color = JournalInk,
                    fontSize = if (isArabicScript(piece.denomination.label) || isRtl) 14.sp else 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(platformStyle = NoFontPadding)
                )

                if (rialText != null) {
                    Text(
                        text = "($rialText)",
                        fontFamily = resolveJournalFont(rialText, isRtl),
                        color = JournalMutedInk,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            val dhSuffix = stringResource(R.string.currency_dirham)
            val isDhLatin = dhSuffix.contains(Regex("[a-zA-Z]"))
            Text(
                text = "$pieceTotalDh $dhSuffix",
                fontFamily = if (isDhLatin) PatrickHandFamily else TajawalFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = JournalWritingInk,
                style = TextStyle(platformStyle = NoFontPadding)
            )
        }

        Spacer(Modifier.height(8.dp))

        val displayCount = piece.count.toInt().coerceAtMost(30)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(displayCount) {
                if (bitmap != null) {
                    if (isBanknote) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "${piece.denomination.label} #${it + 1}",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .width(110.dp)
                                .height(66.dp)
                                .shadow(1.5.dp, RoundedCornerShape(4.dp))
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.65f)),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    } else {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "${piece.denomination.label} #${it + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(54.dp)
                                .shadow(1.5.dp, CircleShape)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.65f)),
                                    CircleShape
                                )
                        )
                    }
                }
            }

            if (piece.count > 30) {
                Box(
                    modifier = Modifier
                        .height(if (isBanknote) 60.dp else 52.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(JournalDockBg)
                        .border(BorderStroke(0.5.dp, JournalRule.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val moreText = "+ ${piece.count - 30} " + if (isRtl) "أخرى" else "autres"
                    Text(
                        text = moreText,
                        fontFamily = resolveJournalFont(moreText, isRtl),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk
                    )
                }
            }
        }
    }
}
