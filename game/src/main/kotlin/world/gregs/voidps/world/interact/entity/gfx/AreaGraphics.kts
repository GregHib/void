import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.batch.addGraphic
import world.gregs.voidps.engine.data.definition.extra.GraphicDefinitions
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.entity.gfx.Graphics
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.Graphic
import world.gregs.voidps.world.interact.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val batches: ChunkBatches by inject()
val definitions: GraphicDefinitions by inject()
val store: EventHandlerStore by inject()

on<World, SpawnGraphic> {
    val graphic = AreaGraphic(tile, Graphic(definitions.get(id).id, delay, height, rotation, forceRefresh), owner)
    store.populate(graphic)
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
    World.run("graphic_${ag.id}_${ag.tile}", ag.graphic.delay / 30) {
        ag.graphic.delay = 0
        graphics.remove(ag)
        val update = ag.update
        if (update != null) {
            batches.removeInitial(ag.tile.chunk, update)
            ag.update = null
        }
        ag.events.emit(Unregistered)
    }
}