package content.skill.melee.weapon.special

import content.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

class StaffOfLight :
    Script,
    SpecialAttack {
    init {
        playerSpawn {
            if (contains("power_of_light")) {
                softTimers.restart("power_of_light")
            }
        }

        timerStart("power_of_light") { 1 }

        timerTick("power_of_light") { if (dec("power_of_light") <= 0) Timer.CANCEL else Timer.CONTINUE }

        timerStop("power_of_light") {
            message("<red>The power of the light fades. Your resistance to melee attacks returns to normal.")
            clear("power_of_light")
        }

        itemRemoved("staff_of_light*", "worn_equipment", EquipSlot.Weapon) {
            softTimers.stop("power_of_light")
        }

        combatDamage {
            if (softTimers.contains("power_of_light")) {
                gfx("power_of_light_impact")
            }
        }

        specialAttackPrepare("power_of_light") { id ->
            if (!SpecialAttack.drain(this)) {
                return@specialAttackPrepare false
            }
            anim("${id}_special")
            gfx("${id}_special")
            set(id, TimeUnit.MINUTES.toTicks(1))
            softTimers.start(id)
            return@specialAttackPrepare false
        }
    }
}
