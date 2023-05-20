package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

/**
 * @param owner Client index if matches client's local index then item won't be displayed
 */
class FloorItemReveal(
    val id: Int,
    val amount: Int,
    val tileOffset: Int,
    val ownerIndex: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.FLOOR_ITEM_REVEAL,
    Protocol.Batch.FLOOR_ITEM_REVEAL,
    7
) {
    override fun visible(name: String) = owner == null || owner == name
}