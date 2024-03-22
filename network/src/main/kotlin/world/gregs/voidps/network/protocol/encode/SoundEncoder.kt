package world.gregs.voidps.network.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.Protocol.JINGLE
import world.gregs.voidps.network.Protocol.MIDI_SOUND
import world.gregs.voidps.network.Protocol.PLAY_MUSIC
import world.gregs.voidps.network.Protocol.SOUND_EFFECT
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.protocol.writeByteInverse
import world.gregs.voidps.network.protocol.writeByteSubtract
import world.gregs.voidps.network.protocol.writeMedium
import world.gregs.voidps.network.protocol.writeShortAddLittle

fun Client.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = send(PLAY_MUSIC) {
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