package com.tajir.sarf.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Neo‑brutalism light (outlined + rounded):
 * - Cards: ~22dp
 * - Buttons: ~18dp (pill buttons use 999dp locally)
 * - Inputs: ~16dp
 */
val SarfShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),     // inputs
    medium = RoundedCornerShape(20.dp),    // buttons / small cards
    large = RoundedCornerShape(22.dp),     // main cards
    extraLarge = RoundedCornerShape(24.dp)
)



