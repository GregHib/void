package content.skill.magic.book.ancient

import content.entity.effect.toxin.poison
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.type.random

class SmokeSpells(val definitions: SpellDefinitions) : Script {

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("smoke_")) {
            return
        }
        if (random.nextDouble() <= 0.2) {
            source.poison(target, definitions.get(spell)["poison_damage"])
        }
    }
}
