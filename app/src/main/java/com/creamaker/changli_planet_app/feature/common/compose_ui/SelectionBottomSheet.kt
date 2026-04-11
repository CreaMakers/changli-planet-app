package com.creamaker.changli_planet_app.feature.common.compose_ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creamaker.changli_planet_app.core.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBottomSheet(
    items: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val colors = AppTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxHeightDp = with(density) {
        (windowInfo.containerSize.height * 0.5f).toDp()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.campusSnowColor,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.campusMistColor) }
    ) {
        LazyColumn(modifier = Modifier
            .heightIn(max = maxHeightDp)
            .padding(bottom = 12.dp)) {
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item); onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = colors.campusInkColor
                )
                if (item != items.last()) {
                    HorizontalDivider(
                        color = colors.campusDividerColor,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}
