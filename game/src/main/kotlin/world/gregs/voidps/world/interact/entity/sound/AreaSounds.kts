import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.batch.addSound
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.sound.AreaSound
import world.gregs.voidps.engine.entity.sound.Sounds
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.sound.PlaySound

val store: EventHandlerStore by inject()
val batches: ChunkBatches by inject()
val sounds: Sounds by inject()

on<World, PlaySound> {
    val sound = AreaSound(tile, id, radius, repeat, delay, volume, speed, midi, owner)
    store.populate(sound)
    batches.add(tile.chunk, addSound(sound))
    sound.events.emit(Registered)
    val duration = 10// TODO duration from definitions
    World.run("sound_${sound.id}_${sound.tile}", (sound.delay + duration * 30) * sound.repeat) {
        sounds.remove(sound)
        sound.events.emit(Unregistered)
    }
}