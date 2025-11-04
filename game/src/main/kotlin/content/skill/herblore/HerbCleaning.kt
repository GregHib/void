package content.skill.herblore

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.data.Cleaning
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class HerbCleaning : Script {

    init {
        itemOption("Clean") { (item, slot) ->
            val herb: Cleaning = item.def.getOrNull("cleaning") ?: return@itemOption
            if (!has(Skill.Herblore, herb.level, true)) {
                return@itemOption
            }

            if (inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
                experience.add(Skill.Herblore, herb.xp)
            }
        }
    }
}
