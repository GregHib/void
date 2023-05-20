package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

class SoundAddition(
    val id: Int,
    val tileOffset: Int,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int,
    val owner: String?
) : ChunkUpdate(
    Protocol.SOUND_AREA,
    Protocol.Batch.SOUND_AREA,
    8
) {
    override fun visible(name: String) = owner == null || owner == name
}