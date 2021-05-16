package world.gregs.voidps.world.interact.entity.sound

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.definition.SoundDefinitions
import world.gregs.voidps.network.encode.playMIDI
import world.gregs.voidps.network.encode.playSoundEffect
import world.gregs.voidps.utility.get

fun Player.playSound(
    name: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: SoundDefinitions = get()
    val id = definitions.getId(name)
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
    val id = definitions.getId(name)
    client?.playSoundEffect(id, delay, volume, speed, repeat)
    if (radius > 0) {
        areaSound(id, tile, radius, repeat, delay, volume, speed)
    }
}

fun Player.playMidi(
    name: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    val definitions: SoundDefinitions = get()
    val id = definitions.getId(name)
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
    val definitions: SoundDefinitions = get()
    val id = definitions.getId(name)
    client?.playMIDI(id, delay, volume, speed, repeat)
    if (radius > 0) {
        areaMidi(id, tile, radius, repeat, delay, volume, speed)
    }
}