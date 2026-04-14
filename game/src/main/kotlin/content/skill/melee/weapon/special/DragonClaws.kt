package content.skill.melee.weapon.special

import content.entity.combat.hit.Damage
import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.type.random

class DragonClaws : Script {

    init {
        specialAttack("slice_and_dice") { target, id ->
            anim("${id}_special")
            gfx("${id}_special")
            val weapon = weapon
            var (hit1, hit2, hit3, hit4) = intArrayOf(0, 0, 0, 0)
            val maxHit = Damage.maximum(this, target, "melee", weapon)
            if (Hit.success(this, target, "melee", weapon, special = true)) {
                // First hit lands: high damage, half, then two hits adding up to the second.
                // e.g. 300-150-70-80
                hit1 = random.nextInt(maxHit / 2, maxHit + 1)
                hit2 = hit1 / 2
                hit3 = random.nextInt(0, hit2 + 1)
                hit4 = hit2 - hit3
            } else if (Hit.success(this, target, "melee", weapon, special = true)) {
                // First misses, second hits: 3rd and 4th each deal half of the 2nd.
                // e.g. 0-300-150-150
                hit2 = random.nextInt(maxHit / 2, maxHit + 1)
                hit3 = hit2 / 2
                hit4 = hit2 - hit3
            } else if (Hit.success(this, target, "melee", weapon, special = true)) {
                // First two miss: 3rd and 4th are regular hits, capped at 75% max.
                // e.g. 0-0-300-300
                val cappedMax = (maxHit * 3) / 4
                hit3 = random.nextInt(cappedMax + 1)
                hit4 = random.nextInt(cappedMax + 1)
            } else if (Hit.success(this, target, "melee", weapon, special = true)) {
                // First three miss, fourth hits with 50% damage boost.
                // e.g. 0-0-0-450
                hit4 = random.nextInt((maxHit * 3) / 2 + 1)
            } else {
                // All four miss: fourth hit almost always lands between 1 and 7.
                hit4 = random.nextInt(1, 8)
            }
            hit(target, damage = hit1)
            hit(target, damage = hit2)
            hit(target, damage = hit3, delay = TICKS.toClientTicks(2))
            hit(target, damage = hit4 ,delay = TICKS.toClientTicks(2))
        }
    }
}
