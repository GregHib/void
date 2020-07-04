import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.Unregistered
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.entity.obj.Objects
import rs.dusk.engine.model.world.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.obj.ReplaceObject
import rs.dusk.world.entity.obj.ReplaceObjectPair
import rs.dusk.world.entity.obj.SpawnObject

val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()

/**
 * Replaces two objects, linking them to the same job so both revert after timeout
 */
ReplaceObjectPair then {
    val firstReplacement = Location(firstReplacement, firstTile, firstOriginal.type, firstRotation)
    val secondReplacement = Location(secondReplacement, secondTile, secondOriginal.type, secondRotation)
    switch(firstOriginal, firstReplacement)
    switch(secondOriginal, secondReplacement)
    // Revert
    if (ticks >= 0) {
        val job = scheduler.add {
            try {
                delay(ticks)
            } finally {
                switch(firstReplacement, firstOriginal)
                switch(secondReplacement, secondOriginal)
            }
        }
        objects.setTimer(firstReplacement, job)
        objects.setTimer(secondReplacement, job)
    }
}

/**
 * Spawns an object, optionally removing after a set time
 */
SpawnObject then {
    val obj = Location(id, tile, type, rotation, owner)

    batcher.update(
        obj.tile.chunkPlane,
        ObjectAddMessage(obj.tile.offset(), obj.id, obj.type, obj.rotation)
    )
    objects.addTemp(obj)
    bus.emit(Registered(obj))
    // Revert
    if (ticks >= 0) {
        objects.setTimer(obj, scheduler.add {
            try {
                delay(ticks)
            } finally {
                batcher.update(
                    obj.tile.chunkPlane,
                    ObjectRemoveMessage(obj.tile.offset(), obj.type, obj.rotation)
                )
                objects.removeTemp(obj)
                bus.emit(Unregistered(obj))
            }
        })
    }
}

/**
 * Replaces one object with another, optionally reverting after a set time
 */
ReplaceObject then {
    val replacement = Location(id, tile, type, rotation)

    switch(original, replacement)
    // Revert
    if (ticks >= 0) {
        objects.setTimer(replacement, scheduler.add {
            try {
                delay(ticks)
            } finally {
                switch(replacement, original)
            }
        })
    }
}

fun switch(original: Location, replacement: Location) {
    batcher.update(
        original.tile.chunkPlane,
        ObjectRemoveMessage(original.tile.offset(), original.type, original.rotation)
    )
    batcher.update(
        replacement.tile.chunkPlane,
        ObjectAddMessage(replacement.tile.offset(), replacement.id, replacement.type, replacement.rotation)
    )
    objects.removeTemp(original)
    objects.addTemp(replacement)
    bus.emit(Unregistered(original))
    bus.emit(Registered(replacement))
}

batcher.addInitial { player, chunkPlane, messages ->
    objects.getAdded(chunkPlane)?.forEach {
        if (it.visible(player)) {
            messages += ObjectAddMessage(it.tile.offset(), it.id, it.type, it.rotation)
        }
    }
    objects.getRemoved(chunkPlane)?.forEach {
        if (it.visible(player)) {
            messages += ObjectRemoveMessage(it.tile.offset(), it.type, it.rotation)
        }
    }
}