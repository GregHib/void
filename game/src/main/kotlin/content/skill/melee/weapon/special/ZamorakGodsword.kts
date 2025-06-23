package content.skill.melee.weapon.special

import content.entity.effect.freeze
import content.entity.player.combat.special.specialAttackDamage
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

specialAttackDamage("ice_cleave") { player ->
    player.freeze(target, TimeUnit.SECONDS.toTicks(20))
}
