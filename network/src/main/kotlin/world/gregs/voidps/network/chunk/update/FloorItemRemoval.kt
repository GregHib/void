package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

class FloorItemRemoval(
    val id: Int,
    val tileOffset: Int,
    val owner: String?
) : ChunkUpdate(3) {
    override fun visible(name: String) = owner == null || owner == name
}