package content.skill.magic.book.ancient

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds

class MiasmicSpells : Script {

    val definitions: SpellDefinitions by inject()

    init {
        combatAttack("magic", handler = ::attack)
        npcCombatAttack(style = "magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (target, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("miasmic_")) {
            return
        }
        val seconds: Int = definitions.get(spell)["effect_seconds"]
        target.start("miasmic", seconds, epochSeconds())
    }
}
