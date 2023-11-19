package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

object Equipment {
    fun bonus(source: Character, target: Character?, type: String, offense: Boolean): Int {
        val character = if (offense) source else target
        val style = if (source is NPC && offense) "att_bonus" else if (type == "range" || type == "magic") type else character?.combatStyle ?: ""
        return if (character is NPC) character.def[if (offense) style else "${style}_def", 0] else character?.getOrNull(if (offense) style else "${style}_def") ?: 0
    }
}