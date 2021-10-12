package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

data class GraphicAddition(
    val id: Int,
    val tileOffset: Int,
    val height: Int,
    val delay: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(7) {
    override fun visible(name: String) = owner == null || owner == name
}