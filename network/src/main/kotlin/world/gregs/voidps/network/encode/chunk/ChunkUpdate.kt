package world.gregs.voidps.network.encode.chunk

abstract class ChunkUpdate(
    val packetId: Int,
    val packetIndex: Int,
    val size: Int
) {
    abstract fun visible(name: String): Boolean

    open fun private(): Boolean = false

}