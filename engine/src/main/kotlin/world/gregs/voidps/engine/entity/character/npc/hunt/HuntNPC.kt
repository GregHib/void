package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Event

data class HuntNPC(
    val mode: String,
    val target: NPC
) : Event