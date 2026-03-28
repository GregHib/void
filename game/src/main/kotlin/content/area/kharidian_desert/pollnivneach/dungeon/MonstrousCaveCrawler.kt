package content.area.kharidian_desert.pollnivneach.dungeon

import content.entity.combat.target
import content.entity.effect.toxin.poisonDamage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class MonstrousCaveCrawler : Script {
    init {
        npcAttack("monstrous_cave_crawler", "melee") {
            if (inc("hit_count") != 2) {
                return@npcAttack
            }
            clear("hit_count")
            val target = target as? Player ?: return@npcAttack
            // Reduce anti-poison resistance
            target.poisonDamage = (target.poisonDamage + 80).coerceAtMost(80)
            target.timers.startIfAbsent("poison")
            target["poison_source"] = this
        }
    }
}
