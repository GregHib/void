package world.gregs.voidps.network.encode

import io.ktor.utils.io.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.sound.AreaSound
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

fun addSound(sound: AreaSound): (Player) -> Unit = { player ->
    if (sound.midi) {
        player.client?.areaMIDI(sound.tile.offset(), sound.id, sound.radius, sound.rotation, sound.delay, sound.volume, sound.speed)
    } else {
        player.client?.areaSound(sound.tile.offset(), sound.id, sound.radius, sound.rotation, sound.delay, sound.volume, sound.speed)
    }
}

fun Client.areaMIDI(
    tile: Int,
    id: Int,
    radius: Int,
    repeat: Int,
    delay: Int,
    volume: Int,
    speed: Int
) = send(Protocol.MIDI_AREA) {
    writeByte(tile)
    writeShort(id)
    writeByte((radius shl 4) or repeat)
    writeByte(delay)
    writeByte(volume)
    writeShort(speed)
}

fun Client.areaSound(
    tile: Int,
    id: Int,
    radius: Int,
    repeat: Int,
    delay: Int,
    volume: Int,
    speed: Int
) = send(Protocol.SOUND_AREA) {
    writeByte(tile)
    writeShort(id)
    writeByte((radius shl 4) or repeat)
    writeByte(delay)
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