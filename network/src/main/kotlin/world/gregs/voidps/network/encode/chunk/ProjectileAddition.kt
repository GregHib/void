package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

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
) : ChunkUpdate(
    Protocol.PROJECTILE_ADD,
    Protocol.Batch.PROJECTILE_ADD,
    16
) {
    override fun visible(name: String) = owner == null || owner == name
}