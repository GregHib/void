package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

class FloorItemRemoval(
    val id: Int,
    val tileOffset: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_REMOVE,
    Protocol.Batch.FLOOR_ITEM_REMOVE,
    3
) {
    override fun visible(name: String) = owner == null || owner == name
}