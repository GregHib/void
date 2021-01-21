package world.gregs.void.engine.entity.character.npc

import world.gregs.void.engine.event.Event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
abstract class NPCEvent : Event<Unit>() {
    abstract val npc: NPC
}
