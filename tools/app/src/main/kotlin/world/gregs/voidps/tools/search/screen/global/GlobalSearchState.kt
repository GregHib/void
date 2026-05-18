package world.gregs.voidps.tools.search.screen.global

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import world.gregs.voidps.cache.Definition

class GlobalSearchState {
    var query by mutableStateOf("")
    var selectedItem by mutableStateOf<Definition?>(null)
    var selectedItemTabLabel by mutableStateOf<String?>(null)
    var collapsedSections by mutableStateOf(emptySet<String>())
}