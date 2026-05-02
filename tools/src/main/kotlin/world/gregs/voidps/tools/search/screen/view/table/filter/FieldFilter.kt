package world.gregs.voidps.tools.search.screen.view.table.filter

data class FieldFilter(
    val fieldName: String,
    val value: String = "",
    val mode: MatchMode = MatchMode.CONTAINS,
)