import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.update.visual.Graphic
import world.gregs.voidps.engine.entity.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.chunk.addGraphic
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.chunk.ChunkUpdate
import world.gregs.voidps.world.interact.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val scheduler: Scheduler by inject()
val batches: ChunkBatches by inject()
val definitions: GraphicDefinitions by inject()

on<World, SpawnGraphic> {
    val graphic = AreaGraphic(tile, Graphic(definitions.get(id).id, delay, height, rotation, forceRefresh), owner)
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
    scheduler.add(ag.graphic.delay / 30, cancelExecution = true) {
        ag.graphic.delay = 0
        graphics.remove(ag)
        ag.remove<ChunkUpdate>("update")?.let {
            batches.removeInitial(ag.tile.chunk, it)
        }
        ag.events.emit(Unregistered)
    }
}