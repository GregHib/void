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

val objects: Objects by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()

/**
 *  [ReplaceObjectPair] is really lazy but saves the headache of issues removing objects which overlap
 */
ReplaceObjectPair then {
    val firstReplacement = Location(firstReplacement, firstTile, firstOriginal.type, firstRotation)
    val secondReplacement = Location(secondReplacement, secondTile, secondOriginal.type, secondRotation)
    switch(firstOriginal, firstReplacement, secondOriginal, secondReplacement)
    // Revert
    if (ticks >= 0) {
        val job = scheduler.add {
            try {
                delay(ticks)
            } finally {
                switch(firstReplacement, firstOriginal, secondReplacement, secondOriginal)
            }
        }
        objects.setTimer(firstReplacement, job)
        objects.setTimer(secondReplacement, job)
    }
}

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

fun switch(firstOriginal: Location, firstReplacement: Location, secondOriginal: Location, secondReplacement: Location) {
    batcher.update(
        firstOriginal.tile.chunkPlane,
        ObjectRemoveMessage(firstOriginal.tile.offset(), firstOriginal.type, firstOriginal.rotation)
    )
    batcher.update(
        secondOriginal.tile.chunkPlane,
        ObjectRemoveMessage(secondOriginal.tile.offset(), secondOriginal.type, secondOriginal.rotation)
    )
    batcher.update(
        firstReplacement.tile.chunkPlane,
        ObjectAddMessage(firstReplacement.tile.offset(), firstReplacement.id, firstReplacement.type, firstReplacement.rotation)
    )
    batcher.update(
        secondReplacement.tile.chunkPlane,
        ObjectAddMessage(secondReplacement.tile.offset(), secondReplacement.id, secondReplacement.type, secondReplacement.rotation)
    )
    objects.removeTemp(firstOriginal)
    objects.removeTemp(secondOriginal)
    objects.addTemp(firstReplacement)
    objects.addTemp(secondReplacement)
    bus.emit(Unregistered(firstOriginal))
    bus.emit(Unregistered(secondOriginal))
    bus.emit(Registered(firstReplacement))
    bus.emit(Registered(secondReplacement))
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