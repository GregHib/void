package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

/**
 * @param owner Client index if matches client's local index then item won't be displayed
 */
data class FloorItemReveal(
    val id: Int,
    val amount: Int,
    val tileOffset: Int,
    val ownerIndex: Int,
    val owner: String?
) : ChunkUpdate(7) {
    override fun visible(name: String) = owner == null || owner == name
}