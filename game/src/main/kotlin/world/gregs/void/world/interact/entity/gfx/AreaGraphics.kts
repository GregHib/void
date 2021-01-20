import world.gregs.void.engine.action.Scheduler
import world.gregs.void.engine.action.delay
import world.gregs.void.engine.entity.Registered
import world.gregs.void.engine.entity.Unregistered
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.update.visual.Graphic
import world.gregs.void.engine.entity.gfx.AreaGraphic
import world.gregs.void.engine.entity.gfx.Graphics
import world.gregs.void.engine.entity.item.offset
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.event.then
import world.gregs.void.engine.map.chunk.ChunkBatcher
import world.gregs.void.network.codec.game.encode.GraphicAreaEncoder
import world.gregs.void.utility.inject
import world.gregs.void.world.interact.entity.gfx.SpawnGraphic

val graphics: Graphics by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()
val batcher: ChunkBatcher by inject()
val encoder: GraphicAreaEncoder by inject()

SpawnGraphic then {
    val ag = AreaGraphic(tile, Graphic(id, delay, height, rotation, forceRefresh), owner)
    graphics.add(ag)
    batcher.update(tile.chunk, ag.toMessage())
    decay(ag)
    bus.emit(Registered(ag))
}

/**
 * Reduces timers to keep approx in sync for players starting to view mid-way through
 */
fun decay(ag: AreaGraphic) {
    scheduler.add {
        try {
            repeat(ag.graphic.delay / 30) {
                delay(1)
                ag.graphic.delay -= 30
            }
            ag.graphic.delay = 0
            delay(1)// TODO delay by definition duration
        } finally {
            graphics.remove(ag)
            bus.emit(Unregistered(ag))
        }
    }
}

fun AreaGraphic.toMessage(): (Player) -> Unit = { player -> encoder.encode(player, tile.offset(), graphic.id, graphic.height, graphic.delay, graphic.rotation) }

batcher.addInitial { player, chunk, messages ->
    graphics[chunk].forEach {
        if (it.visible(player)) {
            messages += it.toMessage()
        }
    }
}