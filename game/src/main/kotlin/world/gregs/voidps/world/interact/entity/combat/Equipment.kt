package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

object Equipment {
    fun bonus(source: Character, target: Character, type: String, offense: Boolean): Int {
        return if (offense) {
            style(source, type, if (source is NPC) "att_bonus" else combatStyle(type, source))
        } else {
            style(target, type, "${combatStyle(type, target)}_def")
        }
    }

    private fun style(character: Character, type: String, style: String = combatStyle(type, character)): Int {
        return if (character is NPC) character.def[style, 0] else character.getOrNull(style) ?: 0
    }

    private fun combatStyle(type: String, character: Character) = if (type == "range" || type == "magic") type else character.combatStyle
}