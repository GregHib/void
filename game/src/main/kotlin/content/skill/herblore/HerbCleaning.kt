package content.skill.herblore

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class HerbCleaning : Script {

    init {
        itemOption("Clean") { (item, slot) ->
            val level = EnumDefinitions.intOrNull("herb_cleaning_level", item.id) ?: return@itemOption
            if (!has(Skill.Herblore, level, true)) {
                return@itemOption
            }

            if (inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
                val xp = EnumDefinitions.int("herb_cleaning_xp", item.id) / 10.0
                experience.add(Skill.Herblore, xp)
            }
        }
    }
}
