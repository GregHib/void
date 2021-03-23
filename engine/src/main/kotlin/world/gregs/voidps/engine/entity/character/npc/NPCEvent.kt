package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.event.Event

/**
 * @author GregHib <greg@gregs.world>
 * @since March 31, 2020
 */
abstract class NPCEvent : Event {
    abstract val npc: NPC
}
