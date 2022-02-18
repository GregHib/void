import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.addSound
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.PlaySound

val batches: ChunkBatches by inject()
val sounds: Sounds by inject()
val scheduler: Scheduler by inject()

on<World, PlaySound> {
    val sound = AreaSound(tile, id, radius, repeat, delay, volume, speed, midi, owner)
    val update = addSound(sound)
    batches.addInitial(tile.chunk, update)
    batches.update(tile.chunk, update)
    sound.events.emit(Registered)
    val duration = 10// TODO duration from definitions
    scheduler.add((sound.delay + duration * 30) * sound.repeat) {
        sounds.remove(sound)
        sound.events.emit(Unregistered)
        batches.removeInitial(tile.chunk, update)
    }
}