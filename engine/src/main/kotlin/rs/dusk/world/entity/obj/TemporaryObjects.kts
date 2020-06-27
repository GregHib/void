import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.ClearObject
import rs.dusk.world.entity.obj.ReplaceObject

val decoder: ObjectDecoder by inject()
val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()

val timers = mutableMapOf<Location, Job>()

ClearObject then {
    val timer = timers[location]
    timer?.cancel("Cancelled by clear.")
    timers.remove(location)
}

ReplaceObject then {
    val def = decoder.getSafe(id)
    val replacement = Location(id, tile, Size(def.sizeX, def.sizeY), type, rotation)

    switch(original, replacement)
    revert(original, replacement, ticks)
}

/**
 * Schedules disappearance after [ticks]
 */
fun revert(original: Location, replace: Location, ticks: Int) {
    if (ticks >= 0) {
        timers[replace] = scheduler.add {
            try {
                delay(ticks)
            } finally {
                switch(replace, original)
            }
        }
    }
}

fun switch(original: Location, replacement: Location) {
    batcher.update(original.tile.chunkPlane, ObjectRemoveMessage(original.tile.offset(), original.type, original.rotation))
    batcher.update(replacement.tile.chunkPlane, ObjectAddMessage(replacement.tile.offset(), replacement.id, replacement.type, replacement.rotation))
    objects.remove(original)
    objects.add(replacement)
    bus.emit(Unregistered(original))
    bus.emit(Registered(replacement))
}

batcher.addInitial { player, chunkPlane, messages ->
    // TODO how to handle removals. What do to with personal objects per player? Cause
    objects[chunkPlane]?.forEach {
        if(it.visible(player)) {
            messages += ObjectAddMessage(it.tile.offset(), it.id, it.type, it.rotation)
        }
    }
}