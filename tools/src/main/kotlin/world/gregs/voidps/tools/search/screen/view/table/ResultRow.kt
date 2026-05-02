package world.gregs.voidps.tools.search.screen.view.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.content_copy
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.BgMultiSelected
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BgSelected
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.copyToClipboard
import world.gregs.voidps.tools.search.displayValue
import kotlin.collections.forEach
import kotlin.reflect.KProperty1


@Composable
fun ResultRow(
    item: Definition,
    isSelected: Boolean,
    isInMultiSelection: Boolean,
    selectedItems: List<Definition>,
    columns: List<KProperty1<Definition, *>>,
    onClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }

    val bgColor = when {
        isSelected -> BgSelected
        isInMultiSelection -> BgMultiSelected
        else -> Color.Transparent
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .pointerInput(showMenu) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press
                                && event.buttons.isSecondaryPressed
                            ) {
                                val pos = event.changes.first().position
                                menuOffset = DpOffset(pos.x.toDp(), 0.dp)  // y=0 so it anchors below the row naturally
                                showMenu = true
                                // consume only the right-click
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
                .clickable(
                    onClick = {
                        // Check shift state via pointer position isn't available here,
                        // so we use a separate hover-tracked shift state (see below)
                        onClick()
                    }
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            columns.forEachIndexed { idx, prop ->
                val raw = try {
                    prop.get(item)
                } catch (_: Exception) {
                    null
                }
                val isParams = prop.name == "params"
                val txt = displayValue(raw, resolveParams = isParams)
                val isId = prop.name == "id"
                Box(modifier = Modifier.weight(if (isId) 0.5f else 1f)) {
                    Text(
                        txt, fontSize = 12.sp, maxLines = 1,
                        color = when {
                            isId -> AccentBlue; txt == "null" || txt == "-1" -> TextMuted; else -> TextPrimary
                        },
                        fontFamily = if (isId) FontFamily.Monospace else FontFamily.Default
                    )
                }
                if (idx < columns.lastIndex) Spacer(Modifier.width(8.dp))
            }
        }

        // Right-click context menu — one entry per visible column
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = menuOffset,
            modifier = Modifier.background(BgPanel).border(0.5.dp, BorderColor, RoundedCornerShape(6.dp)),
        ) {
            val targets = if (selectedItems.size > 1) selectedItems else listOf(item)
            val isMulti = targets.size > 1
            if (isMulti) {
                Text(
                    "${targets.size} rows selected",
                    fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 4.dp)
                )
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
                // Copy all selected as TSV (with header)
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.content_copy), null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                            Text("Copy ${targets.size} rows (TSV)", fontSize = 11.sp, color = AccentBlue)
                        }
                    },
                    onClick = {
                        val header = columns.joinToString("\t") { it.name }
                        val rows = targets.joinToString("\n") { row ->
                            columns.joinToString("\t") { prop ->
                                displayValue(
                                    try {
                                        prop.get(row)
                                    } catch (_: Exception) {
                                        null
                                    }, prop.name == "params"
                                )
                            }
                        }
                        copyToClipboard("$header\n$rows")
                        showMenu = false
                    },
                    modifier = Modifier.height(30.dp),
                )
                // Copy a single field across all selected rows
                Text(
                    "Copy column across selection",
                    fontSize = 10.sp, color = TextMuted,
                    modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 4.dp)
                )
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
            } else {
                Text("Copy value", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 4.dp))
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(bottom = 4.dp))
            }
            columns.forEach { prop ->
                val singleVal = displayValue(
                    try {
                        prop.get(item)
                    } catch (_: Exception) {
                        null
                    },
                    prop.name == "params"
                )
                val copyText = if (isMulti) {
                    targets.joinToString("\n") { row ->
                        displayValue(
                            try {
                                prop.get(row)
                            } catch (_: Exception) {
                                null
                            }, prop.name == "params"
                        )
                    }
                } else singleVal

                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                prop.name, fontSize = 11.sp, color = TextSecond,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.widthIn(min = 60.dp)
                            )
                            Text(
                                if (isMulti) "(${targets.size} values)"
                                else singleVal.take(40) + if (singleVal.length > 40) "…" else "",
                                fontSize = 11.sp, color = if (isMulti) TextSecond else TextPrimary
                            )
                        }
                    },
                    onClick = { copyToClipboard(copyText); showMenu = false },
                    modifier = Modifier.height(30.dp),
                )
            }
            if (!isMulti) {
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.content_copy), null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                            Text("Copy all fields", fontSize = 11.sp, color = AccentBlue)
                        }
                    },
                    onClick = {
                        val all = columns.joinToString("\t") { prop ->
                            displayValue(
                                try {
                                    prop.get(item)
                                } catch (_: Exception) {
                                    null
                                }, prop.name == "params"
                            )
                        }
                        copyToClipboard(all)
                        showMenu = false
                    },
                    modifier = Modifier.height(30.dp),
                )
            }
        }
    }
}
