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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.RGB_565
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyBreakdownSheet(
    totalCentimes: Long,
    onDismiss: () -> Unit
) {
    val pieces = remember(totalCentimes) { MoneyMath.breakdown(totalCentimes) }
    val remainderCentimes = remember(totalCentimes, pieces) {
        val accounted = pieces.sumOf { it.denomination.valueCentimes * it.count }
        (totalCentimes - accounted).coerceAtLeast(0)
    }

    val dirhamFormatted = MoneyMath.fromCentimes(totalCentimes, MoneyUnit.DIRHAM)
    val rialFormatted = MoneyMath.fromCentimes(totalCentimes, MoneyUnit.RIAL)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .background(JournalRule.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.breakdown_title),
                    fontFamily = PatrickHandFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Summary Conversion Band
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .background(JournalDockBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(0.65.dp, JournalRule.copy(alpha = 0.72f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$dirhamFormatted ${stringResource(R.string.currency_dirham)}",
                        fontFamily = ManropeFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorOrange
                    )
                    Text(
                        text = "  =  ",
                        fontFamily = ManropeFamily,
                        color = JournalMutedInk,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "$rialFormatted ${stringResource(R.string.currency_rial)}",
                        fontFamily = ManropeFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorOrange
                    )
                }
            }

            item {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = JournalRule.copy(alpha = 0.54f), thickness = 0.55.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.breakdown_distribution),
                    fontFamily = ManropeFamily,
                    color = JournalWritingInk,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
            }

            pieces.forEach { piece ->
                item(key = piece.denomination.label) {
                    BreakdownRow(piece = piece)
                }
            }

            if (remainderCentimes > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .drawBehind {
                                drawLine(
                                    color = JournalRule.copy(alpha = 0.54f),
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 0.55.dp.toPx()
                                )
                            }
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.breakdown_remainder)} (< 10 centimes):",
                            fontFamily = ManropeFamily,
                            color = JournalInk,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "$remainderCentimes c",
                            fontFamily = ManropeFamily,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(piece: MoneyPiece) {
    val bitmap = rememberBanknoteImage(piece.denomination.assetPath)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .drawBehind {
                drawLine(
                    color = JournalRule.copy(alpha = 0.54f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 0.55.dp.toPx()
                )
            }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${piece.count}",
                fontFamily = ManropeFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorOrange
            )
            Text(
                text = "×",
                fontFamily = ManropeFamily,
                color = JournalMutedInk,
                fontSize = 14.sp
            )
            Text(
                text = piece.denomination.label,
                fontFamily = TajawalFamily,
                color = JournalInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = piece.denomination.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(42.dp)
                    .width(68.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(BorderStroke(0.6.dp, JournalRule.copy(alpha = 0.7f)), RoundedCornerShape(3.dp))
            )
        }
    }
}
