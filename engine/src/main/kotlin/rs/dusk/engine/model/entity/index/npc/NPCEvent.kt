package rs.dusk.engine.model.entity.index.npc

import rs.dusk.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class NPCEvent : Event() {
    abstract val npc: NPC
}
