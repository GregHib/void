package world.gregs.voidps.network.encode.chunk

abstract class ChunkUpdate(
    val packetId: Int,
    val packetIndex: Int,
    val size: Int
) {
    open val private: Boolean = false
    open fun visible(owner: Int): Boolean = false
}