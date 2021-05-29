package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.map.chunk.ChunkUpdate
import world.gregs.voidps.network.*
import world.gregs.voidps.network.Protocol.JINGLE
import world.gregs.voidps.network.Protocol.MIDI_SOUND
import world.gregs.voidps.network.Protocol.PLAY_MUSIC
import world.gregs.voidps.network.Protocol.SOUND_EFFECT

fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.send(PLAY_MUSIC) {
    writeByteSubtract(delay)
    writeByteSubtract(volume)
    writeShortAddLittle(music)
}

fun Client.playSoundEffect(
    sound: Int,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) = send(SOUND_EFFECT) {
    writeShort(sound)
    writeByte(repeat)
    writeShort(delay)
    writeByte(volume)
    writeShort(speed)
}

fun Client.playMIDI(
    sound: Int,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) = send(MIDI_SOUND) {
    writeShort(sound)
    writeByte(repeat)
    writeShort(delay)
    writeByte(volume)
    writeShort(speed)
}

fun Client.playJingle(
    effect: Int,
    volume: Int = 255
) = send(JINGLE) {
    writeMedium(0)
    writeShortAddLittle(effect)
    writeByteInverse(volume)
}

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