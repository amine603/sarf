package com.tajir.sarf

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.MobileAds
import com.tajir.sarf.settings.AppSettings
import com.tajir.sarf.settings.ThemeMode
import com.tajir.sarf.ui.MainScreen
import com.tajir.sarf.ui.theme.SarfTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {}

        setContent {
            val context = LocalContext.current
            val settings = remember(context) { AppSettings(context) }
            val themeMode by settings.themeMode.collectAsState(initial = ThemeMode.LIGHT)

            SarfTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}