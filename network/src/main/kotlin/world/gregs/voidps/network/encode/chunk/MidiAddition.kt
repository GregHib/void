package world.gregs.voidps.network.encode.chunk

import world.gregs.voidps.network.Protocol

data class MidiAddition(
    val id: Int,
    val tileOffset: Int,
    val radius: Int,
    val repeat: Int,
    val delay: Int,
    val volume: Int,
    val speed: Int
) : ChunkUpdate(
    Protocol.MIDI_AREA,
    Protocol.Batch.MIDI_AREA,
    8
)