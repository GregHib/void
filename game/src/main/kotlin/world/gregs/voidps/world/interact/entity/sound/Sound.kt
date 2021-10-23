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
    name: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: SoundDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return
    client?.playSoundEffect(id, delay, volume, speed, repeat)
}

fun Player.playGlobalSound(
    name: String,
    radius: Int = 10,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: SoundDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return
    client?.playSoundEffect(id, delay, volume, speed, repeat)
    if (radius > 0) {
        areaSound(name, tile, radius, repeat, delay, volume, speed)
    }
}

fun Player.playMidi(
    name: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: MidiDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return
    client?.playMIDI(id, delay, volume, speed, repeat)
}

fun Player.playGlobalMidi(
    name: String,
    radius: Int = 10,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: MidiDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return
    client?.playMIDI(id, delay, volume, speed, repeat)
    if (radius > 0) {
        areaMidi(name, tile, radius, repeat, delay, volume, speed)
    }
}

fun Player.playJingle(
    name: String,
    volume: Double = 1.0
) {
    val definitions: JingleDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return
    client?.playJingle(id, (volume.coerceIn(0.0, 1.0) * 255).toInt())
}