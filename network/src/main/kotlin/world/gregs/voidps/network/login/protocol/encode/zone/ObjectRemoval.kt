package world.gregs.voidps.network.login.protocol.encode.zone

import world.gregs.voidps.network.login.Protocol

data class ObjectRemoval(
    val tile: Int,
    val type: Int,
    val rotation: Int,
    val owner: String? = null,
) : ZoneUpdate(
    Protocol.OBJECT_REMOVE,
    Protocol.Batch.OBJECT_REMOVE,
    2,
) {
    override val private = owner != null
    override fun visible(owner: String) = this.owner == null || this.owner == owner
}
