package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class ObjectAnimation(
    val id: Int,
    val tileOffset: Int,
    val type: Int,
    val rotation: Int
) : ChunkUpdate(
    Protocol.OBJECT_ANIMATION_SPECIFIC,
    Protocol.Batch.OBJECT_ANIMATION_SPECIFIC,
    4
)