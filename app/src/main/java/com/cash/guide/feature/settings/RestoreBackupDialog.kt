package com.cash.guide.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cash.guide.R
import com.cash.guide.data.backup.BackupPayload
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RestoreBackupDialog(
    payload: BackupPayload,
    onMerge: () -> Unit,
    onReplace: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val formattedDate = remember(payload.exportedAtEpochMs) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(payload.exportedAtEpochMs))
    }

    val titleFont = if (isRtl) TajawalFamily else PatrickHandFamily
    val bodyFont = if (isRtl) TajawalFamily else PatrickHandFamily

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            LocalLayoutDirection provides layoutDirection
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(14.dp),
                color = JournalPaper,
                border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.8f)),
                tonalElevation = 0.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Text(
                        text = stringResource(R.string.backup_dialog_restore_title),
                        fontFamily = titleFont,
                        fontSize = if (isRtl) 17.sp else 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )

                    // Backup metadata pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JournalRule.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.backup_dialog_restore_info,
                                formattedDate,
                                payload.totalCalculations,
                                payload.totalGroups
                            ),
                            fontFamily = bodyFont,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalWritingInk,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Option 1: Merge
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HighlighterYellow.copy(alpha = 0.22f))
                            .border(1.dp, Color(0xFFD4A62C).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onMerge() }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.backup_dialog_option_merge),
                            fontFamily = titleFont,
                            fontSize = if (isRtl) 15.sp else 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.backup_dialog_merge_desc),
                            fontFamily = bodyFont,
                            fontSize = 13.sp,
                            color = JournalWritingInk,
                            lineHeight = 17.sp
                        )
                    }

                    // Option 2: Replace
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JournalActionDelete.copy(alpha = 0.08f))
                            .border(1.dp, JournalActionDelete.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { onReplace() }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.backup_dialog_option_replace),
                            fontFamily = titleFont,
                            fontSize = if (isRtl) 15.sp else 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalActionDelete
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(R.string.backup_dialog_replace_desc),
                            fontFamily = bodyFont,
                            fontSize = 13.sp,
                            color = JournalWritingInk,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, JournalRule)
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontFamily = bodyFont,
                            color = JournalMutedInk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
