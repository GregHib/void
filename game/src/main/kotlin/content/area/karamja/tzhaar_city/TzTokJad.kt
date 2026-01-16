package content.area.karamja.tzhaar_city

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.entity.combat.target
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.queue.strongQueue

class TzTokJad : Script {
    init {
        npcAttack("tztok_jad", "magic") {
            val target = target ?: return@npcAttack
            // Note: Override for jad only, don't use elsewhere
            strongQueue("hit_target", 3) {
                hit(target, offensiveType = "magic", delay = 64, damage = Damage.roll(this@npcAttack, target, offensiveType = "magic", range = 0..950))
            }
        }

        npcAttack("tztok_jad", "range") {
            val target = target ?: return@npcAttack
            // Note: Override for jad only, don't use elsewhere
            strongQueue("hit_target", 3) {
                hit(target, offensiveType = "range", delay = 64, damage = Damage.roll(this@npcAttack, target, offensiveType = "range", range = 0..970))
            }
        }
    }
}