package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.player.inv.inventoryOption
import java.util.concurrent.TimeUnit

fun isSkillcape(item: Item) = item.def.contains("skill_cape") || item.def.contains("skill_cape_t")

inventoryOption("Boost", "worn_equipment") {
    if (slot != EquipSlot.Cape.index || !isSkillcape(item)) {
        return@inventoryOption
    }
    if (player.hasClock("skillcape_boost_cooldown")) {
        player.message("You've already boosted in the last 60 seconds.") // Custom message
        return@inventoryOption
    }

    val skill: Skill = item.def["skillcape_skill"]
    if (player.levels.getOffset(skill) > 0) {
        player.message("You already have a boost active.") // Custom message
        return@inventoryOption
    }
    player.levels.boost(skill, if (skill == Skill.Constitution) 10 else 1)
    player.start("skillcape_boost_cooldown", TimeUnit.MINUTES.toTicks(1))
}
