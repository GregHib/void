package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
data class FloorItemUpdate(
    val tile: Int,
    val id: Int,
    val stack: Int,
    val combined: Int,
    val owner: Int
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_UPDATE,
    Protocol.Batch.FLOOR_ITEM_UPDATE,
    7
) {
    override val private = true
    override fun visible(owner: Int) = this.owner == 0 || this.owner == owner
}