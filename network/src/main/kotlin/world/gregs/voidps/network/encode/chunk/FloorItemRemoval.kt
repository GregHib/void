package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class FloorItemRemoval(
    val id: Int,
    val tileOffset: Int,
    val owner: String? = null
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_REMOVE,
    Protocol.Batch.FLOOR_ITEM_REMOVE,
    3
) {
    override val private: Boolean
        get() = owner != null
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}