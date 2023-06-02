package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class ObjectAddition(
    val id: Int,
    val tileOffset: Int,
    val type: Int,
    val rotation: Int
) : ChunkUpdate(
    Protocol.OBJECT_ADD,
    Protocol.Batch.OBJECT_ADD,
    4
)