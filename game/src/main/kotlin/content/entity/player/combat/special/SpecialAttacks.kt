package content.entity.player.combat.special

import content.entity.combat.hit.hit
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound

class SpecialAttacks : Script, SpecialAttack {
    init {
        variableSet("special_attack") { _, from, to ->
            if (to == true && from != true) {
                val id: String = weapon.def.getOrNull("special") ?: return@variableSet
                if (!SpecialAttack.prepare(this, id)) {
                    specialAttack = false
                }
            }
        }

        specialAttackPrepare("*") {
            SpecialAttack.hasEnergy(this)
        }

        specialAttack { target, id ->
            anim("${id}_special")
            gfx("${id}_special")
            sound("${id}_special")
            val damage = hit(target)
            if (damage >= 0) {
                target.gfx("${id}_impact")
            }
            SpecialAttack.damage(this, target, id, damage)
        }
    }
}
