package content.entity.player.combat.special

import content.entity.combat.hit.hit
import content.entity.sound.sound
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script

@Script
class SpecialAttacks : Api {

    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (key == "special_attack" && to == true && from != true) {
            val id: String = player.weapon.def.getOrNull("special") ?: return
            val prepare = SpecialAttackPrepare(id)
            player.emit(prepare)
            if (prepare.cancelled) {
                player.specialAttack = false
            }
        }
    }

    init {
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
