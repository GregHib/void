package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

data class FloorItemReveal(
    val tile: Int,
    val id: Int,
    val amount: Int,
    val ownerIndex: Int,
) : ZoneUpdate(
    Protocol.FLOOR_ITEM_REVEAL,
    Protocol.Batch.FLOOR_ITEM_REVEAL,
    7,
)
