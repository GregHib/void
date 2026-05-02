package world.gregs.voidps.tools.search.screen.view.tab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.screen.view.table.filter.FieldFilter
import world.gregs.voidps.tools.search.screen.view.detail.FieldLink

class TabState(
    val label: String,
    val clazz: Class<Definition>,
    val defaultColumns: List<String>,
    val fieldLinks: List<FieldLink>,
) {
    var definitions: List<Definition> by mutableStateOf(emptyList())
    var loading: Boolean by mutableStateOf(true)
    var error: String? by mutableStateOf(null)
    var visibleColumns: List<String> by mutableStateOf(defaultColumns)
    var columnFilters: Map<String, FieldFilter> by mutableStateOf(emptyMap())
    var selectedItems: List<Definition> by mutableStateOf(emptyList())   // multi-select
    var lastClickedIndex: Int by mutableStateOf(-1)                      // for shift-range
}

val TabState.selectedItem: Definition? get() = selectedItems.lastOrNull()