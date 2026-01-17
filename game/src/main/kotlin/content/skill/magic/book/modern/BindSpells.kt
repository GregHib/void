package content.skill.magic.book.modern

import content.entity.effect.freeze
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character

class BindSpells(val definitions: SpellDefinitions) : Script {

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack) {
        val (target, damage, _, _, spell) = attack
        if (damage <= 0 || (spell != "bind" && spell != "snare" && spell != "entangle")) {
            return
        }
        source.freeze(target, definitions.get(spell)["freeze_ticks"])
    }
}
