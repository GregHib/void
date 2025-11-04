package content.skill.prayer.bone

import com.github.michaelbull.logging.InlineLogger
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue

class BoneBurying : Script {

    val logger = InlineLogger()

    init {
        itemOption("Bury") { (item, slot) ->
            if (!item.def.contains("prayer_xp")) {
                return@itemOption
            }
            if (hasClock("bone_delay")) {
                return@itemOption
            }
            val xp = item.def["prayer_xp", 0.0]
            if (xp <= 0.0) {
                logger.warn { "Missing bone xp: ${item.id}" }
                return@itemOption
            }
            message("You dig a hole in the ground.", ChatType.Filter)
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            start("bone_delay", 1)
            anim("human_pickupfloor")
            exp(Skill.Prayer, xp)
            sound("bury_bones")
            set("i_wonder_if_itll_sprout_task", true)
            weakQueue("bury", 1, onCancel = null) {
                message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
            }
        }
    }
}
