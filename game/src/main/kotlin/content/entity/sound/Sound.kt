package content.entity.sound

import world.gregs.voidps.engine.data.definition.JingleDefinitions
import world.gregs.voidps.engine.data.definition.MidiDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect

fun Character.sound(
    id: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1
) {
    if (this !is Player) {
        return
    }
    client?.playSoundEffect(get<SoundDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
}

fun Player.soundGlobal(
    id: String,
    radius: Int = 5,
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