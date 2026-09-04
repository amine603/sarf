package com.cash.guide.ui.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.app.AppDestination

@Composable
fun NotebookBottomNavigation(
    currentDestination: AppDestination,
    onNavigateTo: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = JournalDockBg,
        tonalElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(
                color = JournalRule.copy(alpha = 0.6f),
                thickness = 0.65.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Tab 1: Home
                BottomNavItem(
                    label = stringResource(R.string.nav_home),
                    symbol = HisabiSymbol.Home,
                    isSelected = currentDestination == AppDestination.Home,
                    onClick = { onNavigateTo(AppDestination.Home) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: History
                BottomNavItem(
                    label = stringResource(R.string.nav_history),
                    symbol = HisabiSymbol.Page,
                    isSelected = currentDestination == AppDestination.History,
                    onClick = { onNavigateTo(AppDestination.History) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 3: Settings
                BottomNavItem(
                    label = stringResource(R.string.nav_settings),
                    symbol = HisabiSymbol.More,
                    isSelected = currentDestination == AppDestination.Settings,
                    onClick = { onNavigateTo(AppDestination.Settings) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    symbol: HisabiSymbol,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDesc = if (isSelected) stringResource(R.string.cd_selected) else stringResource(R.string.cd_not_selected)

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) HighlighterPink.copy(alpha = 0.35f) else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .semantics {
                contentDescription = "$label, $selectedDesc"
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HisabiSketchIcon(
                symbol = symbol,
                contentDescription = null,
                tint = if (isSelected) JournalInk else JournalMutedInk,
                size = 18.dp
            )

            Text(
                text = label,
                fontFamily = ManropeFamily,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) JournalInk else JournalMutedInk,
                maxLines = 1
            )
        }
    }
}
