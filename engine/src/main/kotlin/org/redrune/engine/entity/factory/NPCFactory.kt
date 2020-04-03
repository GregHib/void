package org.redrune.engine.entity.factory

import org.redrune.engine.entity.event.Registered
import org.redrune.engine.entity.model.NPC
import org.redrune.engine.event.EventBus
import org.redrune.engine.model.Direction
import org.redrune.engine.model.Tile
import org.redrune.utility.inject

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