package com.cash.guide.ui.notebook

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerDialog(
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    initialMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1, // 1-12
    onSelectMonth: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    val currentCal = remember { Calendar.getInstance() }
    val thisYear = currentCal.get(Calendar.YEAR)
    val thisMonth = currentCal.get(Calendar.MONTH) + 1

    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val frenchMonths = remember {
        listOf(
            "Janvier", "Février", "Mars", "Avril",
            "Mai", "Juin", "Juillet", "Août",
            "Septembre", "Octobre", "Novembre", "Décembre"
        )
    }

    val arabicMonths = remember {
        listOf(
            "يناير", "فبراير", "مارس", "أبريل",
            "ماي", "يونيو", "يوليوز", "غشت",
            "شتنبر", "أكتوبر", "نونبر", "دجنبر"
        )
    }

    val monthNames = if (isRtl) arabicMonths else frenchMonths

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(JournalPaper)
                .border(1.2.dp, JournalRule.copy(alpha = 0.60f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top row: Year Navigation (< 2026 >)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Decrement Year
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JournalRule.copy(alpha = 0.20f))
                            .clickable(role = Role.Button, onClick = { selectedYear-- }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRtl) "→" else "←",
                            fontFamily = PatrickHandFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                    }

                    // Year Title
                    Text(
                        text = "$selectedYear",
                        fontFamily = PatrickHandFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )

                    // Increment Year
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JournalRule.copy(alpha = 0.20f))
                            .clickable(role = Role.Button, onClick = { selectedYear++ }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRtl) "←" else "→",
                            fontFamily = PatrickHandFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                    }
                }

                // Months Grid (4 rows of 3 columns)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (row in 0 until 4) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (col in 0 until 3) {
                                val monthIndex = row * 3 + col
                                val monthNumber = monthIndex + 1
                                val isCurrent = selectedYear == thisYear && monthNumber == thisMonth

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isCurrent) HighlighterPink.copy(alpha = 0.45f)
                                            else JournalPaper
                                        )
                                        .border(
                                            width = if (isCurrent) 1.dp else 0.6.dp,
                                            color = if (isCurrent) HighlighterPink else JournalRule.copy(alpha = 0.40f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable(
                                            role = Role.Button,
                                            onClick = {
                                                onSelectMonth(selectedYear, monthNumber)
                                                onDismiss()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = monthNames[monthIndex],
                                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                        fontSize = if (isRtl) 14.sp else 15.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = JournalInk,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                }
                            }
                        }
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(role = Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isRtl) "إغلاق" else "Fermer",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk
                    )
                }
            }
        }
    }
}
