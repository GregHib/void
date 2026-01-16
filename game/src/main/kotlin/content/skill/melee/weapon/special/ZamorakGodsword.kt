package content.skill.melee.weapon.special

import content.entity.effect.freeze
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class ZamorakGodsword : Script {

    init {
        specialAttackDamage("ice_cleave") { target, damage ->
            if (damage < 0) {
                return@specialAttackDamage
            }
            freeze(target, TimeUnit.SECONDS.toTicks(20))
        }
    }
}
