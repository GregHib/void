package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.entity.character.player.PlayerEvent

data class NPCOption(val npc: NPC, val option: String?, val partial: Boolean) : PlayerEvent