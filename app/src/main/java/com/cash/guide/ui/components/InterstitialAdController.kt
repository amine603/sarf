package com.cash.guide.ui.components

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4182222159500814/6555225430"

/**
 * Minimal controller to preload + show an interstitial safely.
 */
class InterstitialAdController {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading: Boolean = false

    fun preload(context: Context) {
        val appContext = context.applicationContext
        if (isLoading || interstitialAd != null) return
        isLoading = true

        InterstitialAd.load(
            appContext,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                }
            }
        )
    }

    /**
     * Shows the interstitial if ready. Returns true if shown, false otherwise.
     * Automatically clears and preloads the next ad after show/dismiss/failure.
     */
    fun showIfReady(activity: Activity, onDismiss: (() -> Unit)? = null): Boolean {
        val ad = interstitialAd ?: run {
            preload(activity)
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Ad is one-time-use.
                interstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                onDismiss?.invoke()
                preload(activity)
            }

            override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                onDismiss?.invoke()
                preload(activity)
            }
        }

        ad.show(activity)
        return true
    }
}

@Composable
fun rememberInterstitialAdController(isOnline: Boolean): InterstitialAdController {
    val context = LocalContext.current
    val controller = remember { InterstitialAdController() }

    // Best-effort preload whenever we become online (and on first composition if already online).
    DisposableEffect(isOnline, context) {
        if (isOnline) controller.preload(context)
        onDispose { /* no-op */ }
    }

    return controller
}







