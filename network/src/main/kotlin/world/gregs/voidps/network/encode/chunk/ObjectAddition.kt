package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class ObjectAddition(
    val id: Int,
    val tileOffset: Int,
    val type: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.OBJECT_ADD,
    Protocol.Batch.OBJECT_ADD,
    4
) {
    override fun visible(name: String) = owner == null || owner == name
}