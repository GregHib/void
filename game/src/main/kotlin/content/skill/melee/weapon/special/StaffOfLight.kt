package content.skill.melee.weapon.special

import content.entity.combat.hit.combatDamage
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttackPrepare
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

@Script
class StaffOfLight : Api {
    init {
        playerSpawn { player ->
            if (player.contains("power_of_light")) {
                player.softTimers.restart("power_of_light")
            }
        }

        timerStart("power_of_light") { 1 }

        timerTick("power_of_light") { if (dec("power_of_light") <= 0) Timer.CANCEL else Timer.CONTINUE }

        timerStop("power_of_light") {
            message("<red>The power of the light fades. Your resistance to melee attacks returns to normal.")
            clear("power_of_light")
        }

        itemRemoved("staff_of_light*", EquipSlot.Weapon, "worn_equipment") { player ->
            player.softTimers.stop("power_of_light")
        }

        combatDamage { player ->
            if (player.softTimers.contains("power_of_light")) {
                player.gfx("power_of_light_impact")
            }
        }

        specialAttackPrepare("power_of_light") { player ->
            cancel()
            if (!SpecialAttack.drain(player)) {
                return@specialAttackPrepare
            }
            player.anim("${id}_special")
            player.gfx("${id}_special")
            player[id] = TimeUnit.MINUTES.toTicks(1)
            player.softTimers.start(id)
        }
    }
}
