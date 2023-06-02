package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class FloorItemRemoval(
    val tile: Int,
    val id: Int,
    val owner: Int
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_REMOVE,
    Protocol.Batch.FLOOR_ITEM_REMOVE,
    3
) {
    override val private: Boolean
        get() = owner != 0
    override fun visible(owner: Int) = this.owner == 0 || this.owner == owner
}