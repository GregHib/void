import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.Unregistered
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.update.visual.Graphic
import rs.dusk.engine.entity.gfx.AreaGraphic
import rs.dusk.engine.entity.gfx.Graphics
import rs.dusk.engine.entity.item.offset
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.map.chunk.ChunkBatcher
import rs.dusk.network.codec.game.encode.GraphicAreaEncoder
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.gfx.SpawnGraphic

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