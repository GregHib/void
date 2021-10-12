package world.gregs.voidps.network.chunk

abstract class ChunkUpdate(val size: Int) {
    abstract fun visible(name: String): Boolean
}