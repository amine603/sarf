package com.cash.guide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper

@Composable
fun AiVoiceOnboardingDialog(
    onDismiss: () -> Unit
) {
    val currentLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales.get(0)
    val appLang = currentLocale?.language ?: "ar"
    val isFrench = appLang == "fr"
    val isEnglish = appLang == "en"

    val titleText = when {
        isFrench -> "Assistant vocal IA 🎙️"
        isEnglish -> "AI Voice Assistant 🎙️"
        else -> "المساعد الصوتي بالدارجة 🎙️"
    }

    val bodyText = when {
        isFrench -> "Enregistrez vos articles et calculs d'un seul trait par la voix !\n\n" +
                "• Appuyez sur le micro et parlez librement.\n" +
                "• Dès que vous avez terminé, vérifiez et modifiez les prix avant de confirmer."
        isEnglish -> "Record your items and calculations in a single voice flow!\n\n" +
                "• Tap the mic and speak freely.\n" +
                "• When finished, review and edit prices before confirming."
        else -> "دابا تقدر تسجل كاع السلعة أو الحسابات ديالك بأوديو كامل ومسترسل بلا ما تقيد حاجة بحاجة!\n\n" +
                "• برك على الميكروفون وهضر بالدارجة بكل راحة.\n" +
                "• فاش تسالي، غادي تطلع ليك شاشة كتشوف فيها كلشي وتصحح الأثمنة قبل ما تأكد الإضافة."
    }

    val buttonText = when {
        isFrench -> "Compris, c'est parti 👍"
        isEnglish -> "Got it, let's start 👍"
        else -> "فهمت، نبدا دابا 👍"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JournalPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B7A4B).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFF1B7A4B),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = bodyText,
                    fontSize = 13.5.sp,
                    color = JournalMutedInk,
                    textAlign = TextAlign.Start,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1B7A4B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
