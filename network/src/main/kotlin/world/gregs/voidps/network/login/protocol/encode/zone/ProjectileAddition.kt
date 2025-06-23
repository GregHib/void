package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

data class ProjectileAddition(
    val tile: Int,
    val id: Int,
    val index: Int,
    val directionX: Int,
    val directionY: Int,
    val startHeight: Int,
    val endHeight: Int,
    val delay: Int,
    val flightTime: Int,
    val curve: Int,
    val offset: Int,
) : ZoneUpdate(
    Protocol.PROJECTILE_ADD,
    Protocol.Batch.PROJECTILE_ADD,
    16,
)
