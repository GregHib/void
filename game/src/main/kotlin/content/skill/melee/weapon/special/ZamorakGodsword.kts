package content.skill.melee.weapon.special

import world.gregs.voidps.engine.timer.toTicks
import content.entity.effect.freeze
import content.entity.player.combat.special.specialAttackHit
import java.util.concurrent.TimeUnit

specialAttackHit("ice_cleave") { player ->
    player.freeze(target, TimeUnit.SECONDS.toTicks(20))
}