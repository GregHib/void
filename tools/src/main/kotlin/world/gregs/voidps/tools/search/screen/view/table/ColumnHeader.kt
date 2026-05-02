package world.gregs.voidps.tools.search.screen.view.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.arrow_drop_down
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TagText
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.screen.view.tab.TabState
import world.gregs.voidps.tools.search.screen.view.table.filter.FieldFilter
import world.gregs.voidps.tools.search.screen.view.table.filter.MatchMode
import world.gregs.voidps.tools.search.screen.view.table.filter.ModeChip
import world.gregs.voidps.tools.search.screen.view.table.filter.SearchField


@Composable
fun RowScope.ColumnHeader(
    fieldName: String,
    typeStr: String,
    filter: FieldFilter?,
    onFilterChange: (FieldFilter?) -> Unit,
    weight: Float,
    state: TabState,
) {
    var showDropdown by remember { mutableStateOf(false) }
    val hasFilter = filter != null && filter.value.isNotBlank()

    val isParams = fieldName == "params"
    val isArray = typeStr.contains("Array") || typeStr.contains("IntArray")
    val isNumeric = listOf("Int", "Long", "Double", "Float", "Byte", "Short").any { typeStr.contains(it) }

    val modes: List<MatchMode> = when {
        isParams -> listOf(MatchMode.PARAM_KEY, MatchMode.PARAM_VALUE, MatchMode.CONTAINS, MatchMode.NOT_EMPTY)
        isArray -> listOf(MatchMode.HAS_VALUE, MatchMode.CONTAINS, MatchMode.NOT_EMPTY)
        isNumeric -> listOf(MatchMode.EXACT, MatchMode.GREATER_THAN, MatchMode.LESS_THAN, MatchMode.CONTAINS, MatchMode.NOT_NULL)
        else -> listOf(MatchMode.CONTAINS, MatchMode.EXACT, MatchMode.NOT_EMPTY)
    }

    Box(modifier = Modifier.weight(weight)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .clickable { showDropdown = true }
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                fieldName, fontSize = 11.sp,
                color = if (hasFilter) AccentBlue else TextSecond,
                fontWeight = if (hasFilter) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (hasFilter) Box(Modifier.size(6.dp).background(AccentBlue, RoundedCornerShape(3.dp)))
            Icon(painterResource(Res.drawable.arrow_drop_down), null, tint = if (hasFilter) AccentBlue else TextMuted, modifier = Modifier.size(14.dp))
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.width(230.dp).background(BgPanel).border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
        ) {
            Column(Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(TagBg, RoundedCornerShape(3.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text(fieldName, fontSize = 11.sp, color = TagText, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(typeStr, fontSize = 10.sp, color = TextMuted)
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))

                val currentMode = filter?.mode ?: modes.first()
                val noInputNeeded = currentMode == MatchMode.NOT_NULL || currentMode == MatchMode.NOT_EMPTY
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    modes.forEach { m ->
                        ModeChip(
                            label = when (m) {
                                MatchMode.CONTAINS -> "contains"
                                MatchMode.EXACT -> "exact"
                                MatchMode.GREATER_THAN -> ">"
                                MatchMode.LESS_THAN -> "<"
                                MatchMode.HAS_VALUE -> "has"
                                MatchMode.PARAM_KEY -> "key"
                                MatchMode.PARAM_VALUE -> "value"
                                MatchMode.NOT_NULL -> "not null"
                                MatchMode.NOT_EMPTY -> "not empty"
                            },
                            selected = currentMode == m,
                            onClick = {
                                val sentinel = if (m == MatchMode.NOT_NULL || m == MatchMode.NOT_EMPTY) "*" else filter?.value ?: ""
                                onFilterChange(FieldFilter(fieldName, sentinel, m))
                            },
                        )
                    }
                }

                if (isParams && currentMode == MatchMode.PARAM_KEY) {
                    Spacer(Modifier.height(4.dp))
                    Text("name (e.g. AKA) or numeric id", fontSize = 10.sp, color = TextMuted)
                }
                if (!noInputNeeded) {
                    Spacer(Modifier.height(6.dp))

                    SearchField(
                        value = filter?.value ?: "",
                        onValueChange = { v ->
                            if (v.isEmpty()) onFilterChange(null)
                            else onFilterChange(FieldFilter(fieldName, v, currentMode))
                        },
                        placeholder = "filter…",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (hasFilter) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "clear filter", fontSize = 10.sp, color = AccentBlue,
                        modifier = Modifier.clickable { onFilterChange(null) })
                }
                val isSorted = state.sortField == fieldName   // pass state into ColumnHeader
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ModeChip(
                        label = "↑ asc",
                        selected = isSorted && state.sortAscending,
                        onClick = {
                            state.sortField = fieldName
                            state.sortAscending = true
                        }
                    )
                    ModeChip(
                        label = "↓ desc",
                        selected = isSorted && !state.sortAscending,
                        onClick = {
                            state.sortField = fieldName
                            state.sortAscending = false
                        }
                    )
                    if (isSorted) {
                        ModeChip(label = "clear", selected = false, onClick = { state.sortField = null })
                    }
                }
            }
        }
    }
}