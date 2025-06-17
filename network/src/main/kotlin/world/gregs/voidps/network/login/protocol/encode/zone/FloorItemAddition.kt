package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

data class FloorItemAddition(
    val tile: Int,
    val id: Int,
    val amount: Int,
    val owner: String?,
) : ZoneUpdate(
    Protocol.FLOOR_ITEM_ADD,
    Protocol.Batch.FLOOR_ITEM_ADD,
    5,
) {
    override val private = true
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}
