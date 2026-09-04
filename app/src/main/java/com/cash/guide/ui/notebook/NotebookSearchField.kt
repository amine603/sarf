package com.cash.guide.ui.notebook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

@Composable
fun NotebookSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = stringResource(R.string.home_search_placeholder),
    onSearch: () -> Unit = {},
    readOnly: Boolean = false,
    onClickWhenReadOnly: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (readOnly && onClickWhenReadOnly != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClickWhenReadOnly)
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        color = JournalDockBg.copy(alpha = 0.8f),
        border = BorderStroke(0.75.dp, JournalRule.copy(alpha = 0.8f)),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HisabiSketchIcon(
                symbol = HisabiSymbol.Search,
                contentDescription = null,
                tint = JournalMutedInk,
                size = 18.dp
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = ManropeFamily,
                        fontSize = 14.5.sp,
                        color = JournalMutedInk.copy(alpha = 0.7f)
                    )
                }

                if (!readOnly) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = ManropeFamily,
                            fontSize = 14.5.sp,
                            color = JournalInk
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(JournalInk),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() })
                    )
                }
            }

            if (query.isNotEmpty() && !readOnly) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Close,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = JournalMutedInk,
                        size = 14.dp
                    )
                }
            }
        }
    }
}
