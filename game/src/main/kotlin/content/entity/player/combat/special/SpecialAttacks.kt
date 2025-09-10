package content.entity.player.combat.special

import content.entity.combat.hit.hit
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.event.Script
@Script
class SpecialAttacks {

    init {
        specialAttackPrepare("*") { player ->
            if (!SpecialAttack.hasEnergy(player)) {
                cancel()
            }
        }

        variableSet("special_attack", to = true) { player ->
            if (from == true) {
                return@variableSet
            }
            val id: String = player.weapon.def.getOrNull("special") ?: return@variableSet
            val prepare = SpecialAttackPrepare(id)
            player.emit(prepare)
            if (prepare.cancelled) {
                player.specialAttack = false
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
