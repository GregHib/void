package rs.dusk.engine.entity.factory

import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.Direction
import rs.dusk.engine.model.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class NPCFactory {

    private val bus: EventBus by inject()

    fun spawn(id: Int, x: Int, y: Int, plane: Int, direction: Direction): NPC {
        val npc = NPC(id, Tile(x, y, plane))
        bus.emit(Registered(npc))
        return npc
    }
}