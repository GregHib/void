package content.skill.herblore

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class HerbCleaning : Script {

    init {
        itemOption("Clean") { (item, slot) ->
            val herb = Rows.getOrNull("herbs.${item.id}") ?: return@itemOption
            if (!has(Skill.Herblore, herb.int("level"), true)) {
                return@itemOption
            }

            if (inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
                exp(Skill.Herblore, herb.int("xp") / 10.0)
            }
        }
    }
}
