package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

class GraphicAddition(
    val id: Int,
    val tileOffset: Int,
    val height: Int,
    val delay: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.GRAPHIC_AREA,
    Protocol.Batch.GRAPHIC_AREA,
    7
) {
    override fun visible(name: String) = owner == null || owner == name
}