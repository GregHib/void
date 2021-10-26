package world.gregs.voidps.world.interact.entity.sound

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.JingleDefinitions
import world.gregs.voidps.engine.entity.definition.MidiDefinitions
import world.gregs.voidps.engine.entity.definition.SoundDefinitions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.encode.playJingle
import world.gregs.voidps.network.encode.playMIDI
import world.gregs.voidps.network.encode.playSoundEffect

fun Player.playSound(
    id: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    client?.playSoundEffect(get<SoundDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
}

fun Player.playGlobalSound(
    id: String,
    radius: Int = 10,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    client?.playSoundEffect(get<SoundDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
    if (radius > 0) {
        areaSound(id, tile, radius, repeat, delay, volume, speed)
    }
}

fun Player.playMidi(
    id: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    client?.playMIDI(get<MidiDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
}

fun Player.playGlobalMidi(
    id: String,
    radius: Int = 10,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    client?.playMIDI(get<MidiDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
    if (radius > 0) {
        areaMidi(id, tile, radius, repeat, delay, volume, speed)
    }
}

fun Player.playJingle(
    id: String,
    volume: Double = 1.0
) {
    client?.playJingle(get<JingleDefinitions>().getOrNull(id)?.id ?: return, (volume.coerceIn(0.0, 1.0) * 255).toInt())
}