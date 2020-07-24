package rs.dusk.engine.model.entity.character.npc

import rs.dusk.engine.event.Event

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
abstract class NPCEvent : Event<Unit>() {
    abstract val npc: NPC
}
