package world.gregs.voidps.tools.search.screen.view.tab

import androidx.compose.foundation.lazy.LazyListState
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
    var selectedItems: List<Definition> by mutableStateOf(emptyList())
    var lastClickedIndex: Int by mutableStateOf(-1) // for shift-range
    var sortField: String? by mutableStateOf(null)
    var sortAscending: Boolean by mutableStateOf(true)
    val listState: LazyListState = LazyListState()
    var searchIndex: Map<Int, String> by mutableStateOf(emptyMap())
}

val TabState.selectedItem: Definition? get() = selectedItems.lastOrNull()