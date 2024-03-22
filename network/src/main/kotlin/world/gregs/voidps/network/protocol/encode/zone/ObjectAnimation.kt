package world.gregs.voidps.network.protocol.encode.zone

import world.gregs.voidps.network.Protocol

data class ObjectAnimation(
    val tile: Int,
    val id: Int,
    val type: Int,
    val rotation: Int
) : ZoneUpdate(
    Protocol.OBJECT_ANIMATION_SPECIFIC,
    Protocol.Batch.OBJECT_ANIMATION_SPECIFIC,
    4
)