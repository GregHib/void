package world.gregs.voidps.tools.search.screen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BgDark
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BgSelected
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.screen.view.tab.TabState

@Composable
fun OverflowTabBar(
    tabStates: List<TabState>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    // Measure each tab label's natural width before committing to layout
    var moreMenuExpanded by remember { mutableStateOf(false) }
    val moreButtonWidth = 72.dp
    val tabMinWidth = 60.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(BgPanel)
            .border(BorderStroke(0.5.dp, BorderColor))
    ) {
        // Reserve space for the actions slot (Reload + Load path buttons) and more button
        val actionsWidth = 75.dp
        val availableForTabs = maxWidth - actionsWidth - moreButtonWidth

        // Greedily fit tabs left-to-right at tabMinWidth each
        // We use a simple character-width heuristic: ~8.sp per char + 28dp padding
        fun estimateTabWidth(label: String) = (label.length * 8).dp + 28.dp

        val tabWidths = tabStates.map { estimateTabWidth(it.label).coerceAtLeast(tabMinWidth) }
        var consumed = 0.dp
        val visibleCount = tabWidths.indexOfFirst { w ->
            consumed += w
            consumed > availableForTabs
        }.let { if (it == -1) tabStates.size else it }

        val visibleTabs = tabStates.take(visibleCount)
        val overflowTabs = tabStates.drop(visibleCount)
        val overflowContainsSelected = selectedIdx >= visibleCount

        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            // Visible tabs
            visibleTabs.forEachIndexed { idx, state ->
                val isSelected = idx == selectedIdx
                val hasFilters = state.columnFilters.values.any { it.value.isNotBlank() }
                TabItem(state, isSelected, hasFilters) { onSelect(idx) }
            }

            // "More" dropdown — shown if anything overflows
            if (overflowTabs.isNotEmpty()) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .clickable { moreMenuExpanded = true }
                            .background(if (overflowContainsSelected) BgDark else Color.Transparent)
                            .then(
                                if (overflowContainsSelected) Modifier.border(
                                    BorderStroke(2.dp, AccentBlue),
                                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                ) else Modifier
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (overflowContainsSelected) {
                            // Show which overflow tab is active
                            Text(
                                tabStates[selectedIdx].label,
                                fontSize = 13.sp, color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Divider(
                                modifier = Modifier.height(12.dp).width(0.5.dp),
                                color = BorderColor
                            )
                        }
                        Text(
                            if (overflowContainsSelected) "▾" else "+${overflowTabs.size} ▾",
                            fontSize = 12.sp,
                            color = if (overflowContainsSelected) AccentLight else TextSecond
                        )
                    }

                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false },
                        modifier = Modifier
                            .width(180.dp)
                            .background(BgPanel)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
                    ) {
                        overflowTabs.forEachIndexed { i, state ->
                            val realIdx = visibleCount + i
                            val isSelected = realIdx == selectedIdx
                            val hasFilters = state.columnFilters.values.any { it.value.isNotBlank() }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            state.label,
                                            fontSize = 13.sp,
                                            color = if (isSelected) TextPrimary else TextSecond,
                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        when {
                                            state.loading -> CircularProgressIndicator(
                                                color = TextMuted,
                                                modifier = Modifier.size(8.dp),
                                                strokeWidth = 1.5.dp
                                            )
                                            hasFilters -> Box(
                                                Modifier.size(6.dp)
                                                    .background(AccentBlue, RoundedCornerShape(3.dp))
                                            )
                                            isSelected -> Box(
                                                Modifier.size(6.dp)
                                                    .background(AccentLight, RoundedCornerShape(3.dp))
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelect(realIdx)
                                    moreMenuExpanded = false
                                },
                                modifier = Modifier
                                    .background(if (isSelected) BgSelected else Color.Transparent)
                                    .height(36.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            actions()
        }
    }
}

// Extract the individual tab item so both the bar and dropdown share rendering logic:
@Composable
fun TabItem(state: TabState, isSelected: Boolean, hasFilters: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .background(if (isSelected) BgDark else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(
                    BorderStroke(2.dp, AccentBlue),
                    RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                ) else Modifier
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                state.label, fontSize = 13.sp,
                color = if (isSelected) TextPrimary else TextSecond,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            when {
                state.loading -> CircularProgressIndicator(
                    color = TextMuted, modifier = Modifier.size(8.dp), strokeWidth = 1.5.dp
                )
                hasFilters -> Box(Modifier.size(6.dp).background(AccentBlue, RoundedCornerShape(3.dp)))
            }
        }
    }
}