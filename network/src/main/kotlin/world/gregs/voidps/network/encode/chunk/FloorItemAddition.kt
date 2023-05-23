package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class FloorItemAddition(
    val id: Int,
    val amount: Int,
    val tileOffset: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_ADD,
    Protocol.Batch.FLOOR_ITEM_ADD,
    5
) {
    override fun visible(name: String) = owner == null || owner == name
    override fun private(): Boolean = true
}