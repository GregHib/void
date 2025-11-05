package content.skill.magic.spell

import content.area.wilderness.inMultiCombat
import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.directHit
import content.skill.melee.weapon.multiTargets
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class Spells : Script {

    init {
        characterCombatDamage { character ->
            if (spell.isNotBlank()) {
                character.gfx("${spell}_impact")
                character.sound("${spell}_impact")
                source.sound("${spell}_impact")
            }
        }

        combatAttack(type = "magic") { source ->
            if (!target.inMultiCombat) {
                return@combatAttack
            }
            if (spell.endsWith("_burst") || spell.endsWith("_barrage")) {
                val targets = multiTargets(target, 9)
                for (target in targets) {
                    target.directHit(source, random.nextInt(0..damage), type, weapon, spell)
                }
            }
        }
    }
}
