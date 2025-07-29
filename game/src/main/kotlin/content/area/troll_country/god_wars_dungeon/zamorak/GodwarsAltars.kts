package content.area.troll_country.god_wars_dungeon.zamorak

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val areas: AreaDefinitions by inject()

objectOperate("Pray-at", "prayer_*_altar_godwars") {
    if (player.levels.getOffset(Skill.Prayer) >= 0) {
        player.message("You already have full Prayer points.")
        return@objectOperate
    }
    if (player.hasClock("godwars_altar_recharge")) {
        player.message("You must wait a total of 10 minutes before being able to recharge your prayer points.")
        return@objectOperate
    }
    if (player.hasClock("in_combat")) {
        player.message("You cannot recharge your prayer while engaged in combat.")
        return@objectOperate
    }

    val god = target.id.removePrefix("prayer_").removeSuffix("_altar_godwars")
    val bonus = player.equipment.items.count { it.def.getOrNull<String>("god") == god }
    player.levels.set(Skill.Prayer, player.levels.getMax(Skill.Prayer) + bonus)
    player.anim("altar_pray")
    player.message("Your prayer points feel rejuvenated.")
    player.start("godwars_altar_recharge", TimeUnit.MINUTES.toTicks(10))
}

objectOperate("Teleport", "prayer_*_altar_godwars") {
    val god = target.id.removePrefix("prayer_").removeSuffix("_altar_godwars")
    player.tele(areas["${god}_entrance"])
    player.visuals.hits.clear()
    player.message("The god's pity you and allow you to leave the encampment.")
}
