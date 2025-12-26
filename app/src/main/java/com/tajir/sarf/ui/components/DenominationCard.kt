package com.tajir.sarf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
import com.tajir.sarf.utils.ImageLoader

@Composable
fun DenominationCard(
    label: String,
    count: Int,
    assetPath: String,
    isCoin: Boolean,
    modifier: Modifier = Modifier,
    showCountWhenOne: Boolean = false,
    showLabel: Boolean = true,
) {
    // Display PNGs "as-is" (no trimming, no shadow/border/clip).
    val image = rememberAssetImage(assetPath, trimTransparentPadding = false)
    val currentLabel = rememberUpdatedState(label)

    val shape = if (isCoin) CircleShape else RoundedCornerShape(18.dp)

    // Show money as-is: NO container border/line/shadow. Keep badge/label overlays optional.
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        if (image != null) {
            if (isCoin) {
                Image(
                    bitmap = image,
                    contentDescription = currentLabel.value,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                )
            } else {
                Image(
                    bitmap = image,
                    contentDescription = currentLabel.value,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(2.2f)
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.tajir.sarf.R.string.no_image),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Badge ×N (only if count > 1)
        if (count > 1 || showCountWhenOne) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp),
                shape = CircleShape,
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "×$count",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (showLabel) {
            Text(
                text = label,
                modifier = Modifier.align(Alignment.BottomCenter),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun rememberAssetImage(assetPath: String, trimTransparentPadding: Boolean = false): ImageBitmap? {
    val context = LocalContext.current
    val cache = remember { mutableStateMapOf<String, ImageBitmap>() }

    val cacheKey = if (trimTransparentPadding) "$assetPath|trim" else assetPath

    return remember(cacheKey) {
        cache[cacheKey] ?: runCatching {
            ImageLoader.loadImageFromAssets(context, assetPath, trimTransparentPadding = trimTransparentPadding)
        }.getOrNull()?.also { cache[cacheKey] = it }
    }
}


