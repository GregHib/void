package content.entity.player.effect

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

class SkillcapeBoost : Script {

    init {
        itemOption("Boost", inventory = "worn_equipment") { (item, slot) ->
            if (slot != EquipSlot.Cape.index || !isSkillcape(item)) {
                return@itemOption
            }
            if (hasClock("skillcape_boost_cooldown")) {
                message("You've already boosted in the last 60 seconds.") // Custom message
                return@itemOption
            }

            val skill: Skill = item.def["skillcape_skill"]
            if (levels.getOffset(skill) > 0) {
                message("You already have a boost active.") // Custom message
                return@itemOption
            }
            levels.boost(skill, if (skill == Skill.Constitution) 10 else 1)
            start("skillcape_boost_cooldown", TimeUnit.MINUTES.toTicks(1))
        }
    }

    fun isSkillcape(item: Item) = item.def.contains("skill_cape") || item.def.contains("skill_cape_t")
}
