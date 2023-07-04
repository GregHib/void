package world.gregs.voidps.network.encode.zone

import world.gregs.voidps.network.Protocol

data class ObjectRemoval(
    val tile: Int,
    val type: Int,
    val rotation: Int
) : ZoneUpdate(
    Protocol.OBJECT_REMOVE,
    Protocol.Batch.OBJECT_REMOVE,
    2
)