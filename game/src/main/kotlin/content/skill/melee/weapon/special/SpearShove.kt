package content.skill.melee.weapon.special

import content.entity.combat.combatPrepare
import content.entity.combat.hit.hit
import content.entity.effect.freeze
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class SpearShove : Script, SpecialAttack {

    init {
        combatPrepare("melee") { player ->
            if (!player.specialAttack || player.weapon.def["special", ""] != "shove") {
                return@combatPrepare
            }
            if (target.size > 1) {
                player.message("That creature is too large to knock back!")
                cancel()
            } else if (target.hasClock("movement_delay")) {
                player.message("That ${if (target is Player) "player" else "creature"} is already stunned!")
                cancel()
            }
        }

        specialAttack("shove") { target, id ->
            anim("${id}_special")
            gfx("${id}_special")
            val duration = TimeUnit.SECONDS.toTicks(3)
            target.gfx("dragon_spear_stun")
            target.freeze(duration)
            set("delay", duration)
            hit(target, damage = -1) // Hit with no damage so target can auto-retaliate
            val actual = tile
            val direction = target.tile.delta(actual).toDirection()
            val delta = direction.delta
            if (!target.blocked(direction)) {
                target.exactMove(delta, 30, direction.inverse())
            }
        }
    }
}
