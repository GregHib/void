package world.gregs.voidps.tools.search.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import world.gregs.void.tools.generated.resources.Res
import world.gregs.void.tools.generated.resources.folder_open
import world.gregs.void.tools.generated.resources.refresh
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.*
import world.gregs.voidps.tools.search.screen.global.GlobalSearchState
import world.gregs.voidps.tools.search.screen.global.GlobalSearchTab
import world.gregs.voidps.tools.search.screen.view.detail.FieldLink
import world.gregs.voidps.tools.search.screen.view.tab.DefinitionTab
import world.gregs.voidps.tools.search.screen.view.tab.DefinitionTabContent
import world.gregs.voidps.tools.search.screen.view.tab.TabState
import world.gregs.voidps.tools.search.screen.view.table.filter.FieldFilter
import world.gregs.voidps.tools.search.screen.view.table.filter.MatchMode
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.kotlinProperty
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DefinitionBrowser(
    tabs: List<DefinitionTab<*>>,
    onReload: () -> Unit,
    onChangePath: () -> Unit,
) {
    var selectedIdx by remember { mutableStateOf(0) }
    val globalSearchState = remember { GlobalSearchState() }
    val scope = rememberCoroutineScope()

    val tabStates = remember(tabs) {
        tabs.map { tab -> TabState(tab.label, tab.clazz as Class<Definition>, tab.defaultColumns, tab.fieldLinks) }
    }

    LaunchedEffect(tabStates) {
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
                        tabDefinitionIndex[state.label] = state.definitions.groupBy { it.id }
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
            delay(50.milliseconds)
        }
    }

    fun navigateTo(targetLabel: String, filters: Map<String, String>) {
        val idx = tabStates.indexOfFirst { it.label == targetLabel }
        if (idx == -1) return
        selectedIdx = idx + 1
        tabStates[idx].apply {
            val newFilters = filters.entries.fold(columnFilters) { acc, (field, value) ->
                acc + (field to FieldFilter(field, value, MatchMode.EXACT))
            }
            columnFilters = newFilters
            // Ensure all filtered fields are visible
            val missingCols = filters.keys.filter { it !in visibleColumns }
            if (missingCols.isNotEmpty()) visibleColumns = missingCols + visibleColumns
        }
    }

    val allTabState = remember {
        TabState("All", Definition::class.java, emptyList(), emptyList()).also {
            it.loading = false
        }
    }
    val allTabStates = remember(tabStates) { listOf(allTabState) + tabStates }

    MaterialTheme(colors = darkColors(background = BgDark, surface = BgPanel, primary = AccentBlue)) {
        Column(modifier = Modifier.fillMaxSize().background(BgDark)) {
            OverflowTabBar(
                tabStates = allTabStates,
                selectedIdx = selectedIdx,
                onSelect = { selectedIdx = it },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Reload button — unchanged
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
                        // Load path button — unchanged
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
                            Text("Load path", fontSize = 11.sp, color = AccentLight)
                        }
                    }
                }
            )
            when (selectedIdx) {
                0 -> GlobalSearchTab(
                    tabStates = tabStates,
                    globalState = globalSearchState,
                    onNavigate = ::navigateTo,
                )
                else -> DefinitionTabContent(
                    state = tabStates[selectedIdx - 1],
                    onNavigate = ::navigateTo,
                )
            }
        }
    }
}

/** Set once after all tabs finish loading; used by detail panel for name resolution. */
val tabDefinitionIndex: MutableMap<String, Map<Int, List<Definition>>> = mutableMapOf()

@Suppress("UNCHECKED_CAST")
fun resolveDisplayName(tabLabel: String, id: Int, link: FieldLink? = null): String? {
    val indices = tabDefinitionIndex[tabLabel]?.get(id) ?: return null
    val def = if (link == null || link.resolveByFields == listOf("id")) {
        indices.firstOrNull()
    } else {
        val list = indices.filter { def ->
            link.resolveByFields.any { field ->
                val fieldVal = def.javaClass.declaredFields
                    .firstOrNull { it.name == field }
                    ?.also { it.isAccessible = true }
                    ?.get(def)
                    ?.toString()
                fieldVal == id.toString()
            }
        }
        list.firstOrNull()
    }
    def ?: return null
    val props = def.javaClass.declaredFields
        .mapNotNull { it.kotlinProperty }

    fun valueOf(key: String) = props
        .firstOrNull { it.name == key }
        ?.let { (it as? KProperty1<Any, *>)?.get(def)?.toString() }
        ?.takeIf { it.isNotBlank() && it != "null" && it != def.id.toString() }

    val stringId = valueOf("stringId")
    val name = valueOf("name")
    return stringId ?: name
}

fun resolveNavigationFilters(
    link: FieldLink,
    clickedValue: Int,
    sourceDef: Definition?,
): Map<String, String> = link.targetFilters.associate { (targetField, sourceExpr) ->
    val value = if (sourceExpr == "\$self") {
        clickedValue.toString()
    } else {
        // Read the named field from the source definition
        sourceDef?.javaClass?.declaredFields
            ?.firstOrNull { it.name == sourceExpr }
            ?.also { it.isAccessible = true }
            ?.get(sourceDef)
            ?.toString()
            ?: clickedValue.toString()
    }
    targetField to value
}