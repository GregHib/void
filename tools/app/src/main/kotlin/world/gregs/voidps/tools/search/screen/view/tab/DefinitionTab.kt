package world.gregs.voidps.tools.search.screen.view.tab

import world.gregs.voidps.cache.Definition
import world.gregs.voidps.tools.search.screen.view.detail.FieldLink

data class DefinitionTab<T : Definition>(
    val label: String,
    val clazz: Class<T>,
    val defaultColumns: List<String>,
    val fieldLinks: List<FieldLink> = emptyList(),
    /** Labels of tabs that must finish loading before this tab starts */
    val dependsOn: List<String> = emptyList(),
    val loader: suspend () -> List<T>,
)