package content.skill.fletching

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class Oyster(val dropTables: DropTables) : Script {
    init {
        itemOption("Open", "oyster") {
            val table = dropTables.getValue("oysters")
            inventory.replace(it.slot, "oyster", table.roll().first().toItem().id)
            message("You open the oyster shell.", type = ChatType.Filter)
        }
    }
}
