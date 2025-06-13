package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

/**
 * @param stack Previous item stack size
 * @param combined Updated item stack size
 */
data class FloorItemUpdate(
    val tile: Int,
    val id: Int,
    val stack: Int,
    val combined: Int,
    val owner: String?,
) : ZoneUpdate(
    Protocol.FLOOR_ITEM_UPDATE,
    Protocol.Batch.FLOOR_ITEM_UPDATE,
    7,
) {
    override val private = true
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}
