package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.event.Event

data class NPCOption(val npc: NPC, val def: NPCDefinition, val option: String?, val partial: Boolean) : Event