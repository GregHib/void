package content.skill.herblore

import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
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
            if (!questCompleted("druidic_ritual")) {
                // Maybe authentic based on the video: https://www.youtube.com/watch?v=toQLaJ0SIKA it does not say anything about completing Druidic Ritual only what herblore level needed.
                message("You cannot clean this herb. You need a Herblore level of ${herb.int("level")} to attempt this.")
                return@itemOption
            }

            if (!has(Skill.Herblore, herb.int("level"))) {
                // https://www.youtube.com/watch?v=toQLaJ0SIKA
                message("You cannot clean this herb. You need a Herblore level of ${herb.int("level")} to attempt this.")
                return@itemOption
            }

            if (inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
                exp(Skill.Herblore, herb.int("xp") / 10.0)
                val herbName = herb.string("name")
                // https://www.youtube.com/watch?v=STcDTFTtqfs
                message("You clean the dirt from the $herbName.")
            }
        }
    }
}
