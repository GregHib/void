package world.gregs.voidps.tools.search.screen.global

import world.gregs.voidps.cache.Definition

data class GlobalResult(
    val definition: Definition,
    val tabLabel: String,
    val matchedFields: List<String>,
)