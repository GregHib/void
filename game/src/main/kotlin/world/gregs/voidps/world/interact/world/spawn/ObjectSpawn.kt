package world.gregs.voidps.world.interact.world.spawn

data class ObjectSpawn(
    val id: String,
    val x: Int,
    val y: Int,
    val plane: Int = 0,
    val type: Int,
    val rotation: Int = 0,
    val members: Boolean = false
)