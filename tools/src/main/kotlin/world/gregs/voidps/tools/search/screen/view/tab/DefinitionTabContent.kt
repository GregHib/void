package world.gregs.voidps.tools.search.screen.view.tab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.close
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BgDark
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.screen.view.table.ColumnHeader
import world.gregs.voidps.tools.search.screen.view.table.ColumnPickerButton
import world.gregs.voidps.tools.search.screen.view.table.ResultRow
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.copyToClipboard
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.getProperties
import world.gregs.voidps.tools.search.screen.view.table.filter.matchesFilter
import world.gregs.voidps.tools.search.propertyTypeLabel
import world.gregs.voidps.tools.search.screen.view.detail.DetailPanel
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@Composable
fun DefinitionTabContent(state: TabState, onNavigate: (String, Map<String, String>) -> Unit) {
    val clazz = state.clazz as Class<Definition>
    val properties: List<KProperty1<Definition, *>> = remember(clazz) { getProperties(clazz) }
    val allFieldNames = remember(clazz) { properties.map { it.name } }
    val propsByName = remember(clazz) { properties.associateBy { it.name } }
    var isShiftHeld by remember { mutableStateOf(false) }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(28.dp))
                Text("Loading ${state.label}…", fontSize = 13.sp, color = TextSecond)
            }
        }
        return
    }
    if (state.error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${state.error}", color = Color(0xFFE06C75), fontSize = 13.sp)
        }
        return
    }

    val visibleProps: List<KProperty1<Definition, *>> = remember(state.visibleColumns, propsByName) {
        state.visibleColumns.mapNotNull { propsByName[it] }
    }

    val filteredResults: List<Definition> = remember(state.definitions, state.columnFilters, state.sortField, state.sortAscending) {
        val filtered = state.definitions.filter { def ->
            state.columnFilters.values.all { f ->
                if (f.value.isBlank()) return@all true
                val raw = try { propsByName[f.fieldName]?.get(def) } catch (_: Exception) { null }
                matchesFilter(raw, f)
            }
        }
        val sortProp = state.sortField?.let { propsByName[it] } ?: return@remember filtered
        filtered.sortedWith(Comparator { a, b ->
            val av = try { sortProp.get(a) } catch (_: Exception) { null }
            val bv = try { sortProp.get(b) } catch (_: Exception) { null }
            val cmp = compareValues(av, bv)
            if (state.sortAscending) cmp else -cmp
        })
    }

    val activeFilters = state.columnFilters.values.count { it.value.isNotBlank() }

    val listState: LazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    // Keep focused so arrow keys work immediately
    LaunchedEffect(state.label) { focusRequester.requestFocus() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val minPanelWidth = totalWidth * 0.20f
        val maxPanelWidth = totalWidth * 0.80f
        var detailPanelFraction by remember { mutableStateOf(0.30f) }
        val clampedWidth = (totalWidth * detailPanelFraction).coerceIn(minPanelWidth, maxPanelWidth)

        Row(modifier = Modifier.fillMaxSize()) {
            // ── Table ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BgDark)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        isShiftHeld = event.isShiftPressed
                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                        if (event.isCtrlPressed && event.key == Key.C) {
                            if (state.selectedItems.isNotEmpty()) {
                                val rows = state.selectedItems.joinToString("\n") { item ->
                                    visibleProps.joinToString("\t") { prop ->
                                        displayValue(
                                            try {
                                                prop.get(item)
                                            } catch (_: Exception) {
                                                null
                                            }, prop.name == "params"
                                        )
                                    }
                                }
                                copyToClipboard(rows)
                            }
                            return@onKeyEvent true
                        }
                        val currentIndex = state.lastClickedIndex
                        val newIndex = when (event.key) {
                            Key.DirectionDown -> (currentIndex + 1).coerceAtMost(filteredResults.lastIndex)
                            Key.DirectionUp -> (currentIndex - 1).coerceAtLeast(0)
                            else -> return@onKeyEvent false
                        }
                        if (newIndex != currentIndex && filteredResults.isNotEmpty()) {
                            val item = filteredResults[newIndex]
                            if (event.isShiftPressed) {
                                // Extend selection
                                val range = if (newIndex > state.lastClickedIndex)
                                    (state.lastClickedIndex..newIndex)
                                else
                                    (newIndex..state.lastClickedIndex)
                                state.selectedItems = filteredResults.slice(range)
                            } else {
                                state.selectedItems = listOf(item)
                            }
                            state.lastClickedIndex = newIndex
                            scope.launch { listState.scrollToItem(newIndex) }
                            true
                        } else false
                    }
            ) {
                // Toolbar
                Row(
                    modifier = Modifier.fillMaxWidth().background(BgPanel).padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "${filteredResults.size} / ${state.definitions.size}", fontSize = 12.sp,
                        color = if (activeFilters > 0) AccentBlue else TextSecond, fontWeight = FontWeight.Medium
                    )
                    if (state.selectedItems.size > 1) {
                        Text("· ${state.selectedItems.size} selected", fontSize = 12.sp, color = AccentLight)
                    }
                    Text("results", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.weight(1f))
                    if (activeFilters > 0) {
                        Text(
                            "Clear $activeFilters filter${if (activeFilters > 1) "s" else ""}", fontSize = 11.sp, color = AccentBlue,
                            modifier = Modifier.clickable { state.columnFilters = emptyMap() })
                    }
                    ColumnPickerButton(
                        allFields = allFieldNames,
                        visibleColumns = state.visibleColumns,
                        onToggle = { field, show ->
                            state.visibleColumns = if (show) allFieldNames.filter { it in state.visibleColumns || it == field }
                            else state.visibleColumns.filter { it != field }
                        },
                    )
                }

                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

                // Column headers
                Row(
                    modifier = Modifier.fillMaxWidth().background(BgPanel).padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    visibleProps.forEachIndexed { idx, prop ->
                        ColumnHeader(
                            fieldName = prop.name,
                            typeStr = propertyTypeLabel(prop),
                            filter = state.columnFilters[prop.name],
                            onFilterChange = { updated ->
                                state.columnFilters = if (updated == null) state.columnFilters - prop.name
                                else state.columnFilters + (prop.name to updated)
                            },
                            weight = if (prop.name == "id") 0.5f else 1f,
                            state = state,
                        )
                        if (idx < visibleProps.lastIndex) Spacer(Modifier.width(8.dp))
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(filteredResults) { item ->
                        val itemIndex = filteredResults.indexOf(item)
                        val isSelected = state.selectedItems.lastOrNull() == item
                        val isInMulti = item in state.selectedItems
                        ResultRow(
                            item = item,
                            isSelected = isSelected,
                            isInMultiSelection = isInMulti && !isSelected,
                            selectedItems = state.selectedItems,
                            columns = visibleProps,
                            onClick = {
                                if (isShiftHeld) {
                                    if (state.lastClickedIndex == -1) {
                                        state.selectedItems = listOf(item)
                                        state.lastClickedIndex = itemIndex
                                    } else {
                                        val lo = minOf(state.lastClickedIndex, itemIndex)
                                        val hi = maxOf(state.lastClickedIndex, itemIndex)
                                        state.selectedItems = filteredResults.slice(lo..hi)
                                    }
                                    focusRequester.requestFocus()
                                } else {
                                    state.selectedItems = listOf(item)
                                    state.lastClickedIndex = itemIndex
                                    focusRequester.requestFocus()
                                }
                            },
                        )
                        HorizontalDivider(color = BorderColor.copy(alpha = 0.35f), thickness = 0.5.dp)
                    }
                    if (filteredResults.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    if (activeFilters > 0) "No results match current filters" else "No definitions loaded",
                                    color = TextMuted, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Drag handle ───────────────────────────────────────────────────
            state.selectedItem?.let {
                var isDragging by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(if (isDragging) AccentBlue.copy(alpha = 0.4f) else Color.Transparent)
                        .hoverable(remember { MutableInteractionSource() })
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    when (event.type) {
                                        PointerEventType.Press -> isDragging = true
                                        PointerEventType.Release -> isDragging = false
                                        PointerEventType.Move -> if (isDragging) {
                                            val change = event.changes.first()
                                            change.consume()
                                            val dragDelta = change.position.x - change.previousPosition.x
                                            val deltaDp = dragDelta.toDp()
                                            val newWidth = (totalWidth * detailPanelFraction - deltaDp)
                                                .coerceIn(minPanelWidth, maxPanelWidth)
                                            detailPanelFraction = (newWidth / totalWidth).coerceIn(0.20f, 0.80f)
                                        }
                                    }
                                }
                            }
                        }
                        .pointerHoverIcon(PointerIcon.Hand),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            Box(Modifier.size(2.dp).background(if (isDragging) AccentBlue else BorderColor, RoundedCornerShape(1.dp)))
                        }
                    }
                }

                // ── Detail panel ───────────────────────────────────────────────
                state.selectedItem?.let { item ->
                    Column(
                        modifier = Modifier
                            .width(clampedWidth)
                            .fillMaxHeight()
                            .background(BgPanel)
                            .border(BorderStroke(0.5.dp, BorderColor)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Detail", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            if (state.selectedItems.size > 1) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier.background(AccentBlue.copy(0.15f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("${state.selectedItems.size} selected", fontSize = 10.sp, color = AccentLight)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painterResource(Res.drawable.close), null, tint = TextSecond,
                                modifier = Modifier.size(16.dp).clickable { state.selectedItems = emptyList() })
                        }
                        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                        DetailPanel(item, properties, state.fieldLinks, onNavigate)
                    }
                }
            }
        }
    }
}

// Helper for null-safe comparison:
@Suppress("UNCHECKED_CAST")
private fun compareValues(a: Any?, b: Any?): Int = when {
    a == null && b == null -> 0
    a == null -> 1
    b == null -> -1
    a is Comparable<*> -> (a as Comparable<Any>).compareTo(b)
    else -> a.toString().compareTo(b.toString())
}