package world.gregs.voidps.network.encode.zone

import world.gregs.voidps.network.Protocol

data class SoundAddition(
    val tile: Int,
    val id: Int,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int
) : ZoneUpdate(
    Protocol.SOUND_AREA,
    Protocol.Batch.SOUND_AREA,
    8
)