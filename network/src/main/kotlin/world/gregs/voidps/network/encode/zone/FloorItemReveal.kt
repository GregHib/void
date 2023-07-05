package world.gregs.voidps.network.encode.zone

import world.gregs.voidps.network.Protocol

data class FloorItemReveal(
    val tile: Int,
    val id: Int,
    val amount: Int,
    val ownerIndex: Int
) : ZoneUpdate(
    Protocol.FLOOR_ITEM_REVEAL,
    Protocol.Batch.FLOOR_ITEM_REVEAL,
    7
)