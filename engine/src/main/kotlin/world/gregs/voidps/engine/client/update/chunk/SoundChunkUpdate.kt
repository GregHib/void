package world.gregs.voidps.engine.client.update.chunk

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.Protocol

fun addSound(sound: AreaSound): ChunkUpdate = object : ChunkUpdate {
    override val size = 8

    override fun visible(player: Player): Boolean = sound.visible(player)

    override suspend fun encode(writer: ByteWriteChannel) = writer.run {
        writeByte(if (sound.midi) Protocol.Batch.MIDI_AREA else Protocol.Batch.SOUND_AREA)
        writeByte(sound.tile.offset())
        writeShort(sound.id)
        writeByte((sound.radius shl 4) or sound.repeat)
        writeByte(sound.delay)
        writeByte(sound.volume)
        writeShort(sound.speed)
    }
}