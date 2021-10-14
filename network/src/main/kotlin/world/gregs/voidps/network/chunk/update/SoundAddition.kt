package world.gregs.voidps.network.chunk.update

import world.gregs.voidps.network.chunk.ChunkUpdate

data class SoundAddition(
    val id: Int,
    val tileOffset: Int,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int,
    val midi: Boolean,
    val owner: String?
) : ChunkUpdate(8) {
    override fun visible(name: String) = owner == null || owner == name
}