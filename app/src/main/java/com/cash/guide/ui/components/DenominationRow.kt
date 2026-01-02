package com.cash.guide.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Component to display a single denomination row in the breakdown
 * Shows the image and the count (e.g., "200 DHS × 1")
 */
@Composable
fun DenominationRow(
    value: Int,
    count: Int,
    imageBitmap: ImageBitmap?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display image if available
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "$value DHS",
                modifier = Modifier.size(64.dp)
            )
        }
        
        // Display denomination text
        Text(
            text = "$value DHS × $count",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

