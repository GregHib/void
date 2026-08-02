package content.area.asgarnia.taverley

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class CrystalChest(val dropTables: DropTables) : Script {
    init {
        objectOperate("Open", "crystal_chest") { (target) ->
            val table = dropTables.get("crystal_chest_drop_table") ?: return@objectOperate
            if (!inventory.contains("crystal_key")) {
                message("This chest is securely locked shut.")
                sound("locked")
                return@objectOperate
            }
            anim("open_chest")
            sound("locked")
            message("You unlock the chest with your key.", ChatType.Filter)
            target.replace("crystal_chest_open", ticks = 2)
            delay(1)
            if (inventory.remove("crystal_key")) {
                val drops = table.roll()
                for (drop in drops) {
                    val item = drop.toItem()
                    addOrDrop(item.id, item.amount)
                }
                message("You find some treasure in the chest!")
            }
        }
    }
}
