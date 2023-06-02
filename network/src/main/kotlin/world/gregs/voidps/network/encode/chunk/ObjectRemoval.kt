package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class ObjectRemoval(
    val tile: Int,
    val type: Int,
    val rotation: Int
) : ChunkUpdate(
    Protocol.OBJECT_REMOVE,
    Protocol.Batch.OBJECT_REMOVE,
    2
)