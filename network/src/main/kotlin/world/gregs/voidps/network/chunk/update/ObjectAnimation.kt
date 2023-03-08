package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

class ObjectAnimation(
    val id: Int,
    val tileOffset: Int,
    val type: Int,
    val rotation: Int
) : ChunkUpdate(4) {
    override fun visible(name: String) = true
}