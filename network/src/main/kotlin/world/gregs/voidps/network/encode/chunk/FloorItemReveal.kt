package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class FloorItemReveal(
    val tile: Int,
    val id: Int,
    val amount: Int,
    val ownerIndex: Int
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_REVEAL,
    Protocol.Batch.FLOOR_ITEM_REVEAL,
    7
)