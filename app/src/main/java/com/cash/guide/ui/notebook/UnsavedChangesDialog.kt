package com.cash.guide.ui.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cash.guide.R

@Composable
fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current

    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl
    val titleText = stringResource(R.string.unsaved_title)
    val bodyText = stringResource(R.string.unsaved_body)
    val saveText = stringResource(R.string.unsaved_save)
    val discardText = stringResource(R.string.unsaved_discard)
    val continueText = stringResource(R.string.unsaved_continue)

    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            LocalLayoutDirection provides layoutDirection
        ) {
            Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            color = JournalPaper,
            border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.8f)),
            tonalElevation = 0.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = titleText,
                    fontFamily = resolveJournalFont(titleText, isRtl),
                    fontSize = if (isRtl) 17.sp else 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                )

                Text(
                    text = bodyText,
                    fontFamily = resolveJournalFont(bodyText, isRtl),
                    fontSize = if (isRtl) 14.sp else 14.5.sp,
                    color = JournalWritingInk,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Enregistrer (Primary)
                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighlighterPink,
                            contentColor = JournalInk
                        )
                    ) {
                        Text(
                            text = saveText,
                            fontFamily = resolveJournalFont(saveText, isRtl),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isRtl) 14.sp else 14.5.sp
                        )
                    }

                    // 2. Ignorer les modifications (Destructive/Secondary)
                    OutlinedButton(
                        onClick = onDiscard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, JournalActionDelete.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = JournalActionDelete
                        )
                    ) {
                        Text(
                            text = discardText,
                            fontFamily = resolveJournalFont(discardText, isRtl),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isRtl) 13.5.sp else 14.sp
                        )
                    }

                    // 3. Continuer (Neutral text)
                    TextButton(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = continueText,
                            fontFamily = resolveJournalFont(continueText, isRtl),
                            color = JournalMutedInk,
                            fontSize = if (isRtl) 13.5.sp else 14.sp
                        )
                    }
                }
            }
            }
        }
    }
}
