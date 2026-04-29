package content.skill.magic.book.ancient

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BloodSpells : Script {

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (_, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("blood_")) {
            return
        }
        val maxHeal = Tables.int("spells.${spell}.max_heal")
        if (maxHeal <= 0) {
            return
        }
        val health = (damage / 4).coerceAtMost(maxHeal)
        source.levels.restore(Skill.Constitution, health)
    }
}
