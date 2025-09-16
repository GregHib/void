package content.skill.prayer.bone

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.inv.inventoryOption
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue

@Script
class BoneBurying {

    val logger = InlineLogger()

    init {
        inventoryOption("Bury", "inventory") {
            if (!item.def.contains("prayer_xp")) {
                return@inventoryOption
            }
            if (player.hasClock("bone_delay")) {
                return@inventoryOption
            }
            val xp = item.def["prayer_xp", 0.0]
            if (xp <= 0.0) {
                logger.warn { "Missing bone xp: ${item.id}" }
                return@inventoryOption
            }
            player.message("You dig a hole in the ground.", ChatType.Filter)
            if (!player.inventory.remove(slot, item.id)) {
                return@inventoryOption
            }
            player.start("bone_delay", 1)
            player.anim("human_pickupfloor")
            player.exp(Skill.Prayer, xp)
            player.sound("bury_bones")
            player["i_wonder_if_itll_sprout_task"] = true
            player.weakQueue("bury", 1, onCancel = null) {
                player.message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
            }
        }
    }
}
