import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatcher
import world.gregs.voidps.network.encode.addGraphic
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val scheduler: Scheduler by inject()
val batcher: ChunkBatcher by inject()

on<World, SpawnGraphic> {
    val graphic = AreaGraphic(tile, Graphic(id, delay, height, rotation, forceRefresh), owner)
    graphics.add(graphic)
    batcher.update(tile.chunk, addGraphic(graphic))
    decay(graphic)
    graphic.events.emit(Registered)
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(ag: AreaGraphic) {
    scheduler.launch {
        try {
            repeat(ag.graphic.delay / 30) {
                delay(1)
                ag.graphic.delay -= 30
            }
            ag.graphic.delay = 0
            delay(1)// TODO delay by definition duration
        } finally {
            graphics.remove(ag)
            ag.events.emit(Unregistered)
        }
    }
}

batcher.addInitial { player, chunk, messages ->
    graphics[chunk].forEach {
        if (it.visible(player)) {
            messages += addGraphic(it)
        }
    }
}