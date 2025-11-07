package content.skill.ranged.ammo

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.skill.ranged.ammo
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random

class GodArrows : Script {

    init {
        combatAttack("range") { (target, _, type, weapon, _, _, delay) ->
            if (ammo != "saradomin_arrows") {
                return@combatAttack
            }
            val chance = if (weapon.id == "saradomin_bow") 0.2 else 0.1
            if (random.nextDouble() < chance) {
                // water_strike
                val damage = Damage.roll(this, target, type, weapon)
                hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
            }
        }

        combatAttack("range") { (target, delay, type, weapon) ->
            if (ammo != "guthix_arrows") {
                return@combatAttack
            }
            val chance = if (weapon.id == "guthix_bow") 0.2 else 0.1
            if (random.nextDouble() < chance) {
                // earth_strike
                val damage = Damage.roll(this, target, type, weapon)
                hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
            }
        }

        combatAttack("range") { (target, delay, type, weapon) ->
            if (ammo != "zamorak_arrows") {
                return@combatAttack
            }
            val chance = if (weapon.id == "zamorak_bow") 0.2 else 0.1
            if (random.nextDouble() < chance) {
                // fire_strike
                val damage = Damage.roll(this, target, type, weapon)
                hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
            }
        }
    }
}
