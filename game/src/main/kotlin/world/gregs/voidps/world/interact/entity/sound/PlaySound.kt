package world.gregs.voidps.world.interact.entity.sound

import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.data.definition.extra.SoundDefinitions
import world.gregs.voidps.engine.entity.item.floor.offset
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.network.encode.chunk.MidiAddition
import world.gregs.voidps.network.encode.chunk.SoundAddition

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
    val batches: ChunkBatchUpdates = get()
    batches.add(tile.chunk, MidiAddition(definitions.get(id).id, tile.offset(), radius, repeat, delay, volume, speed))
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
    val batches: ChunkBatchUpdates = get()
    batches.add(tile.chunk, SoundAddition(definitions.get(id).id, tile.offset(), radius, repeat, delay, volume, speed))
}