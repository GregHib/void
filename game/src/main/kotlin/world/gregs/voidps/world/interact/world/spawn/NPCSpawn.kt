package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.entity.Direction

data class NPCSpawn(
    val id: String,
    val x: Int,
    val y: Int,
    val plane: Int = 0,
    val delay: Int? = null,
    val direction: Direction = Direction.NONE,
    val members: Boolean = false
)