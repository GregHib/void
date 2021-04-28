package world.gregs.voidps.engine.map.area

data class MapArea(
    val name: String,
    val area: Area,
    val values: Map<String, Any>
)