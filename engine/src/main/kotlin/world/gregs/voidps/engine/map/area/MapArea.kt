package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.entity.Direction

data class MapArea(
    val name: String,
    val area: Area,
    val tags: Set<String>,
    val npcs: List<Spawn>,
    val items: List<Spawn>
) {
    var loaded = false

    data class Spawn(
        val name: String,
        val weight: Int = 1,
        val limit: Int = 1,
        val amount: Int = 1,
        val delay: Int = 0,
        val direction: Direction = Direction.NONE
    )
}