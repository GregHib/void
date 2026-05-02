package world.gregs.voidps.tools.search.screen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.folder_open
import world.gregs.void.tools.generated.resources.refresh
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.AccentBlue
import world.gregs.voidps.tools.search.AccentLight
import world.gregs.voidps.tools.search.BgCard
import world.gregs.voidps.tools.search.BgDark
import world.gregs.voidps.tools.search.BgPanel
import world.gregs.voidps.tools.search.BorderColor
import world.gregs.voidps.tools.search.TextMuted
import world.gregs.voidps.tools.search.TextPrimary
import world.gregs.voidps.tools.search.TextSecond
import world.gregs.voidps.tools.search.screen.view.tab.DefinitionTab
import world.gregs.voidps.tools.search.screen.view.tab.DefinitionTabContent
import world.gregs.voidps.tools.search.screen.view.tab.TabState
import world.gregs.voidps.tools.search.screen.view.table.filter.FieldFilter
import world.gregs.voidps.tools.search.screen.view.table.filter.MatchMode
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty

@Composable
fun DefinitionBrowser(
    tabs: List<DefinitionTab<*>>,
    onReload: () -> Unit,
    onChangePath: () -> Unit,
) {
    var selectedIdx by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val tabStates = remember(tabs) {
        tabs.map { tab -> TabState(tab.label, tab.clazz, tab.defaultColumns, tab.fieldLinks) }
    }

    LaunchedEffect(tabStates) {
        // Build label -> index map
        val labelToIdx = tabs.mapIndexed { i, t -> t.label to i }.toMap()
        // Track which tabs have finished
        val finished = mutableSetOf<String>()
        // We need to repeatedly poll until all are done; use a simple launch-when-ready approach
        val pending = tabs.indices.toMutableSet()

        while (pending.isNotEmpty()) {
            val toStart = pending.filter { idx ->
                val deps = tabs[idx].dependsOn
                deps.all { it in finished }
            }
            if (toStart.isEmpty()) break // circular or impossible dep — break to avoid infinite loop
            toStart.forEach { idx ->
                pending.remove(idx)
                val state = tabStates[idx]
                scope.launch {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        state.definitions = (tabs[idx] as DefinitionTab<Definition>).loader()
                        // Update reverse lookup index
                        tabDefinitionIndex[state.label] = state.definitions.associateBy { it.id }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        state.error = e.message ?: "Unknown error"
                    } finally {
                        state.loading = false
                        finished.add(tabs[idx].label)
                    }
                }
            }
            // Yield and wait for at least one to finish before re-checking deps
            // Simple approach: wait a short tick then retry
            delay(50)
        }
    }

    fun navigateTo(targetLabel: String, filterId: Int) {
        val idx = tabStates.indexOfFirst { it.label == targetLabel }
        if (idx == -1) return
        selectedIdx = idx
        tabStates[idx].apply {
            columnFilters = columnFilters + ("id" to FieldFilter("id", filterId.toString(), MatchMode.EXACT))
            if ("id" !in visibleColumns) visibleColumns = listOf("id") + visibleColumns
        }
    }

    MaterialTheme(colors = darkColors(background = BgDark, surface = BgPanel, primary = AccentBlue)) {
        Column(modifier = Modifier.fillMaxSize().background(BgDark)) {

            // Tab bar + action buttons
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp)
                    .background(BgPanel).border(BorderStroke(0.5.dp, BorderColor)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabStates.forEachIndexed { idx, state ->
                    val isSelected = idx == selectedIdx
                    val hasFilters = state.columnFilters.values.any { it.value.isNotBlank() }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { selectedIdx = idx }
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

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Reload
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BgCard)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                            .clickable { onReload() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painterResource(Res.drawable.refresh), null, tint = TextSecond, modifier = Modifier.size(13.dp))
                        Text("Reload", fontSize = 11.sp, color = TextSecond)
                    }
                    // Load new path
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .border(0.5.dp, AccentBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable { onChangePath() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(painterResource(Res.drawable.folder_open), null, tint = AccentLight, modifier = Modifier.size(13.dp))
                        Text("Load path…", fontSize = 11.sp, color = AccentLight)
                    }
                }
            }

            DefinitionTabContent(
                state = tabStates[selectedIdx],
                onNavigate = ::navigateTo,
            )
        }
    }
}


/** Set once after all tabs finish loading; used by detail panel for name resolution. */
val tabDefinitionIndex: MutableMap<String, Map<Int, Definition>> = mutableMapOf()

fun resolveDefinition(tabLabel: String, id: Int): Definition? =
    tabDefinitionIndex[tabLabel]?.get(id)

fun resolveDisplayName(tabLabel: String, id: Int): String? {
    val def = resolveDefinition(tabLabel, id) ?: return null
    println("Resolve $tabLabel $id $def")
    // Try "stringId" then "name" fields via reflection
    val properties = def.javaClass.declaredFields.mapNotNull { it.kotlinProperty }
    return (properties.firstOrNull { it.name == "stringId" } ?: properties.firstOrNull { it.name == "name" })
        ?.let {
            @Suppress("UNCHECKED_CAST")
            (it as? KProperty1<Any, *>)?.get(def)?.toString()?.takeIf { s -> s.isNotBlank() && s != "null" }
        }
}