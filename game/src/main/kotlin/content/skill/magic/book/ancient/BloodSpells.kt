package content.skill.magic.book.ancient

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class BloodSpells : Script {

    val definitions: SpellDefinitions by inject()

    init {
        combatAttack("magic", handler = ::attack)
    }

    fun attack(source: Character, attack: CombatAttack) {
        val (_, damage, _, _, spell) = attack
        if (damage <= 0 || !spell.startsWith("blood_")) {
            return
        }
        val maxHeal: Int = definitions.get(spell)["max_heal"]
        val health = (damage / 4).coerceAtMost(maxHeal)
        source.levels.restore(Skill.Constitution, health)
    }
}
