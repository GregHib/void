package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class GraphicAddition(
    val id: Int,
    val tileOffset: Int,
    val height: Int,
    val delay: Int,
    val rotation: Int
) : ChunkUpdate(
    Protocol.GRAPHIC_AREA,
    Protocol.Batch.GRAPHIC_AREA,
    7
)