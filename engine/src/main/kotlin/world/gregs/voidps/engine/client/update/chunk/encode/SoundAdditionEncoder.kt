package world.gregs.voidps.engine.client.update.chunk.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.client.update.chunk.update.SoundAddition
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.map.chunk.ChunkEncoder
import world.gregs.voidps.network.Protocol

class SoundAdditionEncoder : ChunkEncoder<SoundAddition> {
    override suspend fun encode(writer: ByteWriteChannel, update: SoundAddition) = writer.run {
        writeByte(if (update.areaSound.midi) Protocol.Batch.MIDI_AREA else Protocol.Batch.SOUND_AREA)
        writeByte(update.areaSound.tile.offset())
        writeShort(update.areaSound.id)
        writeByte((update.areaSound.radius shl 4) or update.areaSound.repeat)
        writeByte(update.areaSound.delay)
        writeByte(update.areaSound.volume)
        writeShort(update.areaSound.speed)
    }
}