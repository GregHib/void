import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.client.update.batch.addGraphic
import world.gregs.voidps.engine.data.definition.extra.GraphicDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.gfx.AreaGraphic
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.Graphic
import world.gregs.voidps.world.interact.entity.gfx.SpawnGraphic

val batches: ChunkBatchUpdates by inject()
val definitions: GraphicDefinitions by inject()
val store: EventHandlerStore by inject()

on<World, SpawnGraphic> {
    val graphic = AreaGraphic(tile, Graphic(definitions.get(id).id, delay, height, rotation, forceRefresh), owner)
    store.populate(graphic)
    batches.add(tile.chunk, addGraphic(graphic))
    decay(graphic)
    graphic.events.emit(Registered)
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(ag: AreaGraphic) {
    World.run("graphic_${ag.id}_${ag.tile}", ag.graphic.delay / 30) {
        ag.graphic.delay = 0
        ag.events.emit(Unregistered)
    }
}