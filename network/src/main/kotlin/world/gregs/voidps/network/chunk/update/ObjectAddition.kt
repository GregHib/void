package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

class ObjectAddition(
    val id: Int,
    val tileOffset: Int,
    val type: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(4) {
    override fun visible(name: String) = owner == null || owner == name
}