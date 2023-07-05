package world.gregs.voidps.network.encode.zone

import world.gregs.voidps.network.Protocol

data class ObjectAddition(
    val tile: Int,
    val id: Int,
    val type: Int,
    val rotation: Int
) : ZoneUpdate(
    Protocol.OBJECT_ADD,
    Protocol.Batch.OBJECT_ADD,
    4
)