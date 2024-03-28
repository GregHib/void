package world.gregs.voidps.world.interact.entity.sound

import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.zone.MidiAddition
import world.gregs.voidps.network.login.protocol.encode.zone.SoundAddition
import world.gregs.voidps.type.Tile

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
    val batches: ZoneBatchUpdates = get()
    batches.add(tile.zone, MidiAddition(tile.id, definitions.get(id).id, radius, repeat, delay, volume, speed))
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
    val batches: ZoneBatchUpdates = get()
    batches.add(tile.zone, SoundAddition(tile.id, definitions.get(id).id, radius, repeat, delay, volume, speed))
}