package world.gregs.voidps.world.activity.skill.herblore

import world.gregs.voidps.engine.data.definition.data.Cleaning
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import content.entity.player.inv.inventoryOption

inventoryOption("Clean", "inventory") {
    val herb: Cleaning = item.def.getOrNull("cleaning") ?: return@inventoryOption
    if (!player.has(Skill.Herblore, herb.level, true)) {
        return@inventoryOption
    }

    if (player.inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
        player.experience.add(Skill.Herblore, herb.xp)
    }
}