package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
data class FloorItemUpdate(
    val id: Int,
    val tileOffset: Int,
    val stack: Int,
    val combined: Int,
    val owner: String? = null
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_UPDATE,
    Protocol.Batch.FLOOR_ITEM_UPDATE,
    7
) {
    override val private = true
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}