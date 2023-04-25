package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

class ProjectileAddition(
    val id: Int,
    val index: Int,
    val tileOffset: Int,
    val directionX: Int,
    val directionY: Int,
    val startHeight: Int,
    val endHeight: Int,
    val delay: Int,
    val flightTime: Int,
    val curve: Int,
    val offset: Int,
    val owner: String?
) : ChunkUpdate(16) {
    override fun visible(name: String) = owner == null || owner == name
}