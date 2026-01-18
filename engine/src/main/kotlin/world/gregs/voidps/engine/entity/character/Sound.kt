package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.JingleDefinitions
import world.gregs.voidps.engine.data.definition.MidiDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.playJingle
import world.gregs.voidps.network.login.protocol.encode.playMIDI
import world.gregs.voidps.network.login.protocol.encode.playSoundEffect
import world.gregs.voidps.network.login.protocol.encode.zone.MidiAddition
import world.gregs.voidps.network.login.protocol.encode.zone.SoundAddition
import world.gregs.voidps.type.Tile

fun Character.sound(
    id: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1,
) {
    if (this !is Player) {
        return
    }
    val definition = get<SoundDefinitions>().getOrNull(id) ?: return
    if (definition.contains("area")) {
        areaSound(id, tile, radius = 5, delay = delay, volume = volume, speed = speed, repeat = repeat)
    } else {
        client?.playSoundEffect(definition.id, delay, volume, speed, repeat)
    }
}

fun Player.soundGlobal(
    id: String,
    radius: Int = 5,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1,
) {
    client?.playSoundEffect(get<SoundDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
    if (radius > 0) {
        areaSound(id, tile, radius, repeat, delay, volume, speed)
    }
}

fun areaSound(
    id: String,
    tile: Tile,
    radius: Int = 5,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
) {
    val definitions: SoundDefinitions = get()
    val definition = definitions.getOrNull(id) ?: return
    ZoneBatchUpdates.add(tile.zone, SoundAddition(tile.id, definition.id, radius, repeat, delay, volume, speed))
}

fun Player.midi(
    id: String,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1,
) {
    client?.playMIDI(get<MidiDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
}

fun Player.midiGlobal(
    id: String,
    radius: Int = 10,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
    repeat: Int = 1,
) {
    client?.playMIDI(get<MidiDefinitions>().getOrNull(id)?.id ?: return, delay, volume, speed, repeat)
    if (radius > 0) {
        areaMidi(id, tile, radius, repeat, delay, volume, speed)
    }
}

fun areaMidi(
    id: String,
    tile: Tile,
    radius: Int,
    repeat: Int = 1,
    delay: Int = 0,
    volume: Int = 255,
    speed: Int = 255,
) {
    val definitions: SoundDefinitions = get()
    ZoneBatchUpdates.add(tile.zone, MidiAddition(tile.id, definitions.get(id).id, radius, repeat, delay, volume, speed))
}

fun Player.jingle(
    id: String,
    volume: Double = 1.0,
) {
    client?.playJingle(get<JingleDefinitions>().getOrNull(id)?.id ?: return, (volume.coerceIn(0.0, 1.0) * 255).toInt())
}
