package world.gregs.voidps.network.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.chunk.ChunkEncoder
import world.gregs.voidps.network.chunk.update.SoundAddition

class SoundAdditionEncoder : ChunkEncoder<SoundAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: SoundAddition) = writer.run {
        writeByte(if (update.midi) Protocol.Batch.MIDI_AREA else Protocol.Batch.SOUND_AREA)
        writeByte(update.tileOffset)
        writeShort(update.id)
        writeByte((update.radius shl 4) or update.repeat)
        writeByte(update.delay)
        writeByte(update.volume)
        writeShort(update.speed)
    }
}