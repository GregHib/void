package world.gregs.voidps.network.encode.zone

import world.gregs.voidps.network.Protocol

data class GraphicAddition(
    val tile: Int,
    val id: Int,
    val height: Int,
    val delay: Int,
    val rotation: Int
) : ZoneUpdate(
    Protocol.GRAPHIC_AREA,
    Protocol.Batch.GRAPHIC_AREA,
    7
)