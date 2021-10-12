package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
data class FloorItemUpdate(
    val id: Int,
    val tileOffset: Int,
    val stack: Int,
    val combined: Int,
    val owner: String?
) : ChunkUpdate(7) {
    override fun visible(name: String) = owner == null || owner == name
}