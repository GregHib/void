package content.skill.hunter

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class ImplingJars(val dropTables: DropTables) : Script {
    init {
        itemOption("Loot", "*_impling_jar") { (item) ->
            val tables = dropTables.get(Implings.dropTable(this, item.id)) ?: return@itemOption
            val drops = tables.roll(player = this)
            if (inventory.spaces < 2) {
                message("You'll need to clear some space in your pack before looting the jar.")
                return@itemOption
            }
            if (!inventory.remove(item.id)) {
                return@itemOption
            }
            var jar = false
            for (drop in drops) {
                val item = drop.toItem()
                if (item.id == "impling_jar") {
                    jar = true
                }
                addOrDrop(item.id, item.amount)
            }
            if (!jar) {
                message("You break the jar as you try to open it. You throw the shattered remains away.", ChatType.Filter)
            }
        }
    }
}