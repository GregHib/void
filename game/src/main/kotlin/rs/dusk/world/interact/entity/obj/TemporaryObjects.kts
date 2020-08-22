import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.GameObjectFactory
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.network.rs.codec.game.encode.message.ObjectAddMessage
import rs.dusk.network.rs.codec.game.encode.message.ObjectRemoveMessage
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.obj.ReplaceObject
import rs.dusk.world.interact.entity.obj.ReplaceObjectPair

val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val factory: GameObjectFactory by inject()

/**
 * Replaces two objects, linking them to the same job so both revert after timeout
 */
ReplaceObjectPair then {
    val firstReplacement = factory.spawn(firstReplacement, firstTile, firstOriginal.type, firstRotation)
    val secondReplacement = factory.spawn(secondReplacement, secondTile, secondOriginal.type, secondRotation)
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
 * Replaces one object with another, optionally reverting after a set time
 */
ReplaceObject then {
    val replacement = factory.spawn(id, tile, type, rotation)

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

fun switch(original: GameObject, replacement: GameObject) {
    batcher.update(
        original.tile.chunk,
        ObjectRemoveMessage(original.tile.offset(), original.type, original.rotation)
    )
    batcher.update(
        replacement.tile.chunk,
        ObjectAddMessage(replacement.tile.offset(), replacement.id, replacement.type, replacement.rotation)
    )
    objects.removeTemp(original)
    objects.addTemp(replacement)
    bus.emit(Unregistered(original))
    bus.emit(Registered(replacement))
}

batcher.addInitial { player, chunk, messages ->
    objects.getAdded(chunk)?.forEach {
        if (it.visible(player)) {
            messages += ObjectAddMessage(it.tile.offset(), it.id, it.type, it.rotation)
        }
    }
    objects.getRemoved(chunk)?.forEach {
        if (it.visible(player)) {
            messages += ObjectRemoveMessage(it.tile.offset(), it.type, it.rotation)
        }
    }
}