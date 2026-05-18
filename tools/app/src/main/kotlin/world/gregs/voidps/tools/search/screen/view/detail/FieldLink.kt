package world.gregs.voidps.tools.search.screen.view.detail

data class FieldLink(
    val fieldName: String,       // field on source, e.g. "runSound"
    val targetTabLabel: String,  // label of destination tab, e.g. "Sounds"
    val targetFilters: List<Pair<String, String>> = listOf("id" to "\$self"),
    val resolveByFields: List<String> = listOf("id"),
)