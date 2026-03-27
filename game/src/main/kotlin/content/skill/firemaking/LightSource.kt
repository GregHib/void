package content.skill.firemaking

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class LightSource : Script {
    init {
        val unlitSources = Tables.get("light_source").rows().joinToString(",") { it.itemId }
        itemOnItem("tinderbox*", unlitSources) { _, toItem ->
            val source = Rows.getOrNull("light_source.${toItem.id}") ?: return@itemOnItem
            if (!has(Skill.Firemaking, source.int("level"), true)) {
                return@itemOnItem
            }
            if (!inventory.replace(toItem.id, source.item("lit"))) {
                return@itemOnItem
            }
            message("You light the ${source.string("type")}.", ChatType.Game)
        }

        itemOption("Extinguish") { (item) ->
            val extinguished = Tables.itemOrNull("extinguish.${item.id}") ?: return@itemOption
            if (!inventory.replace(item.id, extinguished)) {
                return@itemOption
            }
            message("You extinguish the flame.", ChatType.Game)
        }
    }
}
