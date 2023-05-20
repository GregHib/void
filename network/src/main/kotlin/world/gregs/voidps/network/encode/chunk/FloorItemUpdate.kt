package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
class FloorItemUpdate(
    val id: Int,
    val tileOffset: Int,
    val stack: Int,
    val combined: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_UPDATE,
    Protocol.Batch.FLOOR_ITEM_UPDATE,
    7
) {
    override fun visible(name: String) = owner == null || owner == name
}