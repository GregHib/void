package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

class FloorItemAddition(
    val id: Int,
    val amount: Int,
    val tileOffset: Int,
    val owner: String?
) : ChunkUpdate(5) {
    override fun visible(name: String) = owner == null || owner == name
}