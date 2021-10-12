package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

data class ObjectRemoval(
    val tileOffset: Int,
    val type: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(2) {
    override fun visible(name: String) = owner == null || owner == name
}