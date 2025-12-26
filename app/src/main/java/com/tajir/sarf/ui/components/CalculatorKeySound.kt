package com.tajir.sarf.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants

/**
 * System-like click sound (matches Android UI touch/key clicks; closer to what users hear while typing).
 *
 * Note: This intentionally does NOT use DTMF/telephone tones.
 * It respects the user's system "Touch sounds" setting.
 */
@Composable
fun rememberCalculatorKeySound(): (String) -> Unit {
    // Keep LocalContext read to ensure the composable re-composes correctly across configuration,
    // even though we currently only need the View.
    LocalContext.current
    val view = LocalView.current

    return remember(view) {
        { _: String ->
            runCatching { view.playSoundEffect(SoundEffectConstants.CLICK) }
        }
    }
}


