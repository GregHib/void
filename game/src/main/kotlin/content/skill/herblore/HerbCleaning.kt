package content.skill.herblore

import content.entity.player.inv.inventoryOption
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.data.Cleaning
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class HerbCleaning : Script {

    init {
        inventoryOption("Clean", "inventory") {
            val herb: Cleaning = item.def.getOrNull("cleaning") ?: return@inventoryOption
            if (!player.has(Skill.Herblore, herb.level, true)) {
                return@inventoryOption
            }

            if (player.inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
                player.experience.add(Skill.Herblore, herb.xp)
            }
        }
    }
}
