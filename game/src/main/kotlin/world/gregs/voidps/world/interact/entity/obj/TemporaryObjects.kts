import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.item.offset
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjectFactory
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.codec.game.encode.ObjectAddEncoder
import world.gregs.voidps.network.codec.game.encode.ObjectRemoveEncoder
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.obj.ReplaceObject
import world.gregs.voidps.world.interact.entity.obj.ReplaceObjectPair

val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val factory: GameObjectFactory by inject()
val addEncoder: ObjectAddEncoder by inject()
val removeEncoder: ObjectRemoveEncoder by inject()

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
        val job = scheduler.launch {
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

    switch(gameObject, replacement)
    // Revert
    if (ticks >= 0) {
        objects.setTimer(replacement, scheduler.launch {
            try {
                delay(ticks)
            } finally {
                switch(replacement, gameObject)
            }
        })
    }
}

fun switch(original: GameObject, replacement: GameObject) {
    if (original.tile != replacement.tile) {
        batcher.update(original.tile.chunk) { player ->
            removeEncoder.encode(player, original.tile.offset(), original.type, original.rotation)
        }
    }
    batcher.update(replacement.tile.chunk) { player ->
        addEncoder.encode(player, replacement.tile.offset(), replacement.id, replacement.type, replacement.rotation)
    }
    if (original.tile != replacement.tile) {
        objects.removeTemp(original)
    } else {
        objects.removeAddition(original)
    }
    objects.addTemp(replacement)
    bus.emit(Unregistered(original))
    bus.emit(Registered(replacement))
}

batcher.addInitial { player, chunk, messages ->
    objects.getAdded(chunk)?.forEach {
        if (it.visible(player)) {
            messages += { player ->
                addEncoder.encode(player, it.tile.offset(), it.id, it.type, it.rotation)
            }
        }
    }
    objects.getRemoved(chunk)?.forEach {
        if (it.visible(player)) {
            messages += { player ->
                removeEncoder.encode(player, it.tile.offset(), it.type, it.rotation)
            }
        }
    }
}