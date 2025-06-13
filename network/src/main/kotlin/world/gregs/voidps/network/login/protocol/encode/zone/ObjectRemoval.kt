package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

data class ObjectRemoval(
    val tile: Int,
    val type: Int,
    val rotation: Int,
) : ZoneUpdate(
    Protocol.OBJECT_REMOVE,
    Protocol.Batch.OBJECT_REMOVE,
    2,
)
