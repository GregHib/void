package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class FloorItemAddition(
    val id: Int,
    val amount: Int,
    val tileOffset: Int,
    val owner: String? = null
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_ADD,
    Protocol.Batch.FLOOR_ITEM_ADD,
    5
) {
    override val private = true
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}