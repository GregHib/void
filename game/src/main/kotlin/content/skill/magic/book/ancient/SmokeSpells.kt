package content.skill.magic.book.ancient

import content.entity.effect.toxin.poison
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.type.random

class SmokeSpells : Script {

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("smoke_")) {
            return
        }
        if (random.nextDouble() <= 0.2) {
            val damage = Tables.int("spells.${spell}.poison_damage")
            if (damage <= 0) {
                return
            }
            source.poison(target, damage)
        }
    }
}
