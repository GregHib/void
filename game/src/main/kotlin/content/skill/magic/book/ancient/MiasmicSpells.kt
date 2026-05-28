package content.skill.magic.book.ancient

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.timer.epochSeconds

class MiasmicSpells : Script {

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("miasmic_")) {
            return
        }
        val seconds = Tables.int("spells.$spell.effect_seconds")
        if (seconds <= 0) {
            return
        }
        target.start("miasmic", seconds, epochSeconds())
    }
}
