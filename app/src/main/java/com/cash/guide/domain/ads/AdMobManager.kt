package com.cash.guide.domain.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdMobManager private constructor(private val appContext: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    fun initialize() {
        try {
            MobileAds.initialize(appContext) {
                Log.d(TAG, "Google Mobile Ads SDK initialized successfully")
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun loadRewardedAd() {
        if (rewardedAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            appContext,
            TEST_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    isAdLoading = false
                    _isReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message} (code: ${error.code})")
                    rewardedAd = null
                    isAdLoading = false
                    _isReady.value = false
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad not ready yet, attempting to reload")
            loadRewardedAd()
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed")
                rewardedAd = null
                _isReady.value = false
                loadRewardedAd() // Preload the next one immediately
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Failed to show rewarded ad: ${error.message}")
                rewardedAd = null
                _isReady.value = false
                loadRewardedAd()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed fullscreen content")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        }
    }

    companion object {
        private const val TAG = "AdMobManager"

        // Official Google AdMob Test Unit IDs (100% safe for development/testing)
        const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        @Volatile
        private var instance: AdMobManager? = null

        fun getInstance(context: Context): AdMobManager {
            return instance ?: synchronized(this) {
                instance ?: AdMobManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
