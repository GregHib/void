package content.entity.player.combat.special

import content.entity.combat.hit.hit
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script

class SpecialAttacks : Script {

    init {
        variableSet("special_attack") { player, _, from, to ->
            if (to == true && from != true) {
                val id: String = player.weapon.def.getOrNull("special") ?: return@variableSet
                val prepare = SpecialAttackPrepare(id)
                player.emit(prepare)
                if (prepare.cancelled) {
                    player.specialAttack = false
                }
            }
        }

        specialAttackPrepare("*") { player ->
            if (!SpecialAttack.hasEnergy(player)) {
                cancel()
            }
        }

        specialAttack { player ->
            player.anim("${id}_special")
            player.gfx("${id}_special")
            player.sound("${id}_special")
            val damage = player.hit(target)
            if (damage >= 0) {
                target.gfx("${id}_impact")
            }
            player.emit(SpecialAttackDamage(id, target, damage))
        }
    }
}
