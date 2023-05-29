package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class ObjectRemoval(
    val tileOffset: Int,
    val type: Int,
    val rotation: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.OBJECT_REMOVE,
    Protocol.Batch.OBJECT_REMOVE,
    2
) {
    override fun visible(name: String) = owner == null || owner == name
}