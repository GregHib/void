import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.encode.addSound
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.sound.PlaySound

val batcher: ChunkBatcher by inject()
val sounds: Sounds by inject()

on<World, PlaySound> {
    val sound = AreaSound(tile, id, radius, repeat, delay, volume, speed, midi, owner)
    batcher.update(tile.chunk, addSound(sound))
    sound.events.emit(Registered)
}

batcher.addInitial { player, chunk, messages ->
    sounds[chunk].forEach {
        if (it.visible(player)) {
            messages += addSound(it)
        }
    }
}