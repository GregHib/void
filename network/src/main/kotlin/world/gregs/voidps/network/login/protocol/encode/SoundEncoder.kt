package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.Protocol.JINGLE
import world.gregs.voidps.network.login.Protocol.MIDI_SOUND
import world.gregs.voidps.network.login.Protocol.PLAY_MUSIC
import world.gregs.voidps.network.login.Protocol.SOUND_EFFECT
import world.gregs.voidps.network.login.protocol.*

fun Client.playMusicTrack(
    music: Int,
    delay: Int = 100,
    volume: Int = 255
) = send(PLAY_MUSIC) {
    p1Alt3(delay)
    ip2(music)
    p1Alt2(volume)
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
    p2Alt2(effect)
    p3Alt1(0) //TODO Delay
    p1Alt2(volume)
}