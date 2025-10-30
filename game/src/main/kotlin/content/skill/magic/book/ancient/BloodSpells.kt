package content.skill.magic.book.ancient

import content.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class BloodSpells : Script {

    val definitions: SpellDefinitions by inject()

    init {
        characterCombatAttack(spell = "blood_*", type = "magic") { source ->
            if (damage <= 0) {
                return@characterCombatAttack
            }
            val maxHeal: Int = definitions.get(spell)["max_heal"]
            val health = (damage / 4).coerceAtMost(maxHeal)
            source.levels.restore(Skill.Constitution, health)
        }
    }
}
