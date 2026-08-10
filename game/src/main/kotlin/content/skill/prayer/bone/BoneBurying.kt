package content.skill.prayer.bone

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue

class BoneBurying : Script {

    val logger = InlineLogger()

    init {
        itemOption("Bury") { (item, slot) ->
            val xp = Tables.intOrNull("bones.${item.id}.xp") ?: return@itemOption
            if (hasClock("bone_delay")) {
                return@itemOption
            }
            message("You dig a hole in the ground.", ChatType.Filter)
            if (!inventory.remove(slot, item.id)) {
                return@itemOption
            }
            start("bone_delay", 1)
            anim("climb_down")
            exp(Skill.Prayer, xp / 10.0)
            sound("bury_bones")
            set("i_wonder_if_itll_sprout_task", true)
            strongQueue("bury", 2) {
                message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
            }
        }
    }
}
