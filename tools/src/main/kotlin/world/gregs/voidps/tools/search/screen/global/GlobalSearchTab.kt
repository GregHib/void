package world.gregs.voidps.tools.search.screen.global

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.arrow_drop_down
import world.gregs.void.tools.generated.resources.arrow_right
import world.gregs.void.tools.generated.resources.close
import world.gregs.void.tools.generated.resources.open_in_new
import world.gregs.void.tools.generated.resources.search
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BgDark
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BgSelected
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.LinkColor
import world.gregs.voidps.tools.search.TagBg
import world.gregs.voidps.tools.search.TagText
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.displayValue
import world.gregs.voidps.tools.search.getProperties
import world.gregs.voidps.tools.search.screen.view.detail.DetailPanel
import world.gregs.voidps.tools.search.screen.view.resolveDisplayName
import world.gregs.voidps.tools.search.screen.view.tab.TabState
import kotlin.reflect.KProperty1
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GlobalSearchTab(
    tabStates: List<TabState>,
    globalState: GlobalSearchState,
    onNavigate: (targetLabel: String, filters: Map<String, String>) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Debounce the query so we're not scanning everything on every keystroke
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(globalState.query) {
        if (globalState.query.isBlank()) {
            debouncedQuery = ""; return@LaunchedEffect
        }
        delay(150.milliseconds)
        debouncedQuery = globalState.query
    }

    var results by remember { mutableStateOf(emptyList<GlobalResult>()) }
    var searching by remember { mutableStateOf(false) }
    LaunchedEffect(debouncedQuery, tabStates.map { it.searchIndex }) {
        if (debouncedQuery.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        searching = true
        results = withContext(Dispatchers.Default) {
            val q = debouncedQuery.trim().lowercase()
            tabStates
                .filter { !it.loading && it.error == null && it.searchIndex.isNotEmpty() }
                .flatMap { state ->
                    val matchingDefs = state.definitions.filter { def ->
                        state.searchIndex[def.id]?.contains(q) == true
                    }
                    if (matchingDefs.isEmpty()) {
                        return@flatMap emptyList()
                    }
                    val props = getProperties(state.clazz)
                    matchingDefs.map { def ->
                        val matched = props.filter { prop ->
                            displayValue(
                                try {
                                    prop.get(def)
                                } catch (_: Exception) {
                                    null
                                },
                                prop.name == "params"
                            ).lowercase().contains(q)
                        }.map { it.name }
                        GlobalResult(def, state.label, matched)
                    }
                }
                // Sort: name/stringId matches first, then id matches, then others
                .sortedWith(
                    compareBy(
                        { if (it.matchedFields.any { f -> f == "name" || f == "stringId" }) 0 else 1 },
                        { if (it.matchedFields.contains("id")) 0 else 1 },
                        { it.tabLabel },
                        { it.definition.id },
                    )
                )
                .take(500)  // cap results to keep the list snappy
        }
        searching = false
    }

    // Which tab's properties to show in the detail panel for the selected item
    val selectedTabState = globalState.selectedItemTabLabel
        ?.let { label -> tabStates.firstOrNull { it.label == label } }

    @Suppress("UNCHECKED_CAST")
    val selectedProperties: List<KProperty1<Definition, *>> = remember(selectedTabState?.clazz) {
        selectedTabState?.let { getProperties(it.clazz) } ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDark)) {

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgPanel)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painterResource(Res.drawable.search), null,
                tint = if (globalState.query.isNotBlank()) AccentBlue else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .background(BgCard, RoundedCornerShape(6.dp))
                    .border(
                        0.5.dp,
                        if (globalState.query.isNotBlank()) AccentBlue.copy(0.5f) else BorderColor,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (globalState.query.isEmpty()) {
                    Text("Search all definitions…", color = TextMuted, fontSize = 13.sp)
                }
                BasicTextField(
                    value = globalState.query,
                    onValueChange = { globalState.query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(AccentBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            if (globalState.query.isNotEmpty()) {
                Text(
                    "Clear", fontSize = 12.sp, color = AccentBlue,
                    modifier = Modifier.clickable {
                        globalState.query = ""
                        globalState.selectedItem = null
                    }
                )
            }
        }

        Divider(color = BorderColor, thickness = 0.5.dp)

        val grouped = results.groupBy { it.tabLabel }
        // Status row
        if (debouncedQuery.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPanel)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "${results.size}${if (results.size == 500) "+" else ""} results",
                    fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.Medium
                )
                Text("for", fontSize = 12.sp, color = TextMuted)
                Box(
                    Modifier
                        .background(TagBg, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(debouncedQuery, fontSize = 12.sp, color = TagText, fontFamily = FontFamily.Monospace)
                }
                Spacer(Modifier.weight(1f))
                val allCollapsed = grouped.keys.all { it in globalState.collapsedSections }
                Text(
                    if (allCollapsed) "Expand all" else "Collapse all",
                    fontSize = 11.sp, color = AccentBlue,
                    modifier = Modifier.clickable {
                        globalState.collapsedSections = if (allCollapsed) emptySet()
                        else grouped.keys.toSet()
                    }
                )
                Text("across ${tabStates.count { !it.loading }} tabs", fontSize = 11.sp, color = TextMuted)
            }
            Divider(color = BorderColor, thickness = 0.5.dp)
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = maxWidth
            val minPanelWidth = totalWidth * 0.20f
            val maxPanelWidth = totalWidth * 0.80f
            var detailFraction by remember { mutableStateOf(0.35f) }
            val clampedWidth = (totalWidth * detailFraction).coerceIn(minPanelWidth, maxPanelWidth)

            Row(modifier = Modifier.fillMaxSize()) {
                // Results list
                LazyColumn(modifier = Modifier.weight(1f).fillMaxHeight()) {


                    if (debouncedQuery.isBlank() || searching) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (searching) {
                                        CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                    }
                                    Text("Search across all tabs", fontSize = 14.sp, color = TextSecond)
                                    Text(
                                        "Matches name, stringId, id, and all other fields",
                                        fontSize = 12.sp, color = TextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        // Group by tab label
                        grouped.forEach { (tabLabel, groupResults) ->
                            val isCollapsed = tabLabel in globalState.collapsedSections
                            // Group header
                            stickyHeader {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BgPanel)
                                        .clickable {
                                            globalState.collapsedSections = if (isCollapsed)
                                                globalState.collapsedSections - tabLabel
                                            else
                                                globalState.collapsedSections + tabLabel
                                        }
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        painterResource(if (isCollapsed) Res.drawable.arrow_right else Res.drawable.arrow_drop_down),
                                        if (isCollapsed) "expand" else "collapse",
                                        tint = TextSecond,
                                        modifier = Modifier.width(12.dp)
                                    )
                                    Box(
                                        Modifier
                                            .background(TagBg, RoundedCornerShape(3.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tabLabel, fontSize = 11.sp, color = TagText)
                                    }
                                    Text(
                                        "${groupResults.size} match${if (groupResults.size != 1) "es" else ""}",
                                        fontSize = 11.sp, color = TextMuted
                                    )
                                }
                                Divider(color = BorderColor, thickness = 0.5.dp)
                            }

                            if (!isCollapsed) {
                                items(groupResults) { result ->
                                    val isSelected = globalState.selectedItem == result.definition
                                    val def = result.definition
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) BgSelected else Color.Transparent)
                                            .clickable {
                                                globalState.selectedItem = def
                                                globalState.selectedItemTabLabel = result.tabLabel
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        // ID
                                        Text(
                                            "#${def.id}", fontSize = 12.sp,
                                            color = AccentBlue, fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.width(48.dp)
                                        )

                                        // Name / stringId
                                        val displayName = resolveDisplayName(result.tabLabel, def.id)
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (displayName != null) {
                                                Text(displayName, fontSize = 13.sp, color = TextPrimary, maxLines = 1)
                                            } else {
                                                result.matchedFields
                                            }
                                            // Show which non-obvious fields matched
                                            val interestingFields = result.matchedFields
                                                .filter { it != "id" && it != "name" && it != "stringId" }
                                            if (interestingFields.isNotEmpty()) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    interestingFields.take(4).forEach { field ->
                                                        Box(
                                                            Modifier
                                                                .background(AccentBlue.copy(0.1f), RoundedCornerShape(2.dp))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(field, fontSize = 10.sp, color = AccentLight)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // "Open in tab" button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(BgCard)
                                                .border(0.5.dp, BorderColor, RoundedCornerShape(3.dp))
                                                .clickable {
                                                    onNavigate(
                                                        result.tabLabel,
                                                        mapOf("id" to def.id.toString())
                                                    )
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                "→ ${result.tabLabel}",
                                                fontSize = 11.sp, color = TextSecond
                                            )
                                        }
                                    }
                                    Divider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }

                        if (results.size == 500) {
                            item {
                                Box(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Results capped at 500 — refine your query",
                                        fontSize = 12.sp, color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Detail panel — reuses the same DetailPanel composable
                globalState.selectedItem?.let { item ->
                    // Drag handle
                    var isDragging by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(if (isDragging) AccentBlue.copy(0.4f) else Color.Transparent)
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
                                                val newWidth = (totalWidth * detailFraction - change.position.x.toDp() + change.previousPosition.x.toDp())
                                                    .coerceIn(minPanelWidth, maxPanelWidth)
                                                detailFraction = (newWidth / totalWidth).coerceIn(0.20f, 0.80f)
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

                    Column(
                        modifier = Modifier
                            .width(clampedWidth)
                            .fillMaxHeight()
                            .background(BgPanel)
                            .border(BorderStroke(0.5.dp, BorderColor))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Detail", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            globalState.selectedItemTabLabel?.let { label ->
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier
                                        .background(TagBg, RoundedCornerShape(3.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(label, fontSize = 11.sp, color = TagText)
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Icon(
                                painterResource(Res.drawable.close), null, tint = TextSecond,
                                modifier = Modifier.size(16.dp).clickable { globalState.selectedItem = null }
                            )
                        }
                        Divider(color = BorderColor, thickness = 0.5.dp)
                        if (selectedProperties.isNotEmpty()) {
                            DetailPanel(
                                item = item,
                                properties = selectedProperties,
                                fieldLinks = selectedTabState?.fieldLinks ?: emptyList(),
                                onNavigate = onNavigate,
                            )
                        }
                    }
                }
            }
        }
    }
}