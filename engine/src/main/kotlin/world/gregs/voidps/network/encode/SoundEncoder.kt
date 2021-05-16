package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Protocol
import world.gregs.voidps.network.Protocol.MIDI_SOUND
import world.gregs.voidps.network.Protocol.PLAY_MUSIC
import world.gregs.voidps.network.Protocol.SOUND_EFFECT
import world.gregs.voidps.network.writeByteSubtract
import world.gregs.voidps.network.writeShortAddLittle

fun Player.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = client?.send(PLAY_MUSIC) {
    writeByteSubtract(delay)
    writeByteSubtract(volume)
    writeShortAddLittle(music)
}

fun Player.playSoundEffect(
    sound: Int,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) = client?.send(SOUND_EFFECT) {
    writeShort(sound)
    writeByte(repeat)
    writeShort(delay)
    writeByte(volume)
    writeShort(speed)
}

fun Player.playMIDI(
    sound: Int,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) = client?.send(MIDI_SOUND) {
    writeShort(sound)
    writeByte(repeat)
    writeShort(delay)
    writeByte(volume)
    writeShort(speed)
}

fun Client.areaSound(
    tile: Int,
    id: Int,
    type: Int,
    rotation: Int,
    delay: Int,
    volume: Int,
    speed: Int
) = send(Protocol.SOUND_AREA) {
    writeByte(tile)
    writeShort(id)
    writeByte((type shl 4) and rotation)
    writeByte(delay)
    writeByte(volume)
    writeShort(speed)
}