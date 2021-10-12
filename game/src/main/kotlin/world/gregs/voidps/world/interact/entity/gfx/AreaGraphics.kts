import world.gregs.voidps.engine.action.Scheduler
import world.gregs.voidps.engine.action.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.addGraphic
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.world.interact.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val scheduler: Scheduler by inject()
val batches: ChunkBatches by inject()

on<World, SpawnGraphic> {
    val graphic = AreaGraphic(tile, Graphic(id, delay, height, rotation, forceRefresh), owner)
    graphics.add(graphic)
    val update = addGraphic(graphic)
    graphic["update"] = update
    batches.addInitial(tile.chunk, update)
    batches.update(tile.chunk, update)
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
            ag.remove<ChunkUpdate>("update")?.let {
                batches.removeInitial(ag.tile.chunk, it)
            }
            ag.events.emit(Unregistered)
        }
    }
}