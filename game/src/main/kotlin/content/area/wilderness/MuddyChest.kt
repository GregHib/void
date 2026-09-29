package content.area.wilderness

import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class MuddyChest(private val dropTables: DropTables) : Script {

    init {
        objectOperate("Open", "wilderness_muddy_chest_closed") { (target) ->
            openAndLoot(this, target)
        }

        itemOnObjectOperate("muddy_key", "wilderness_muddy_chest_closed") { (target) ->
            openAndLoot(this, target)
        }

        objectOperate("Open", "wilderness_muddy_chest_open") {
            message("You search the chest but find nothing.")
        }

        objectOperate("Search", "wilderness_muddy_chest_open") {
            message("You search the chest but find nothing.")
        }
    }

    private suspend fun openAndLoot(player: Player, target: GameObject) {
        val table = dropTables.get("muddy_chest_drop_table") ?: return
        if (!player.inventory.contains("muddy_key")) {
            player.sound("locked")
            player.message("This chest is locked.")
            return
        }
        if (!player.inventory.remove("muddy_key")) {
            return
        }
        player.anim("open_chest")
        player.sound("unlock")
        player.message("You unlock the chest with your key.")
        player.delay(1)
        player.sound("chest_open")
        target.replace("wilderness_muddy_chest_open")
        loot(player, table)
        player.message("You find some treasure in the chest!")
        player.delay(1)
        GameObjects.findOrNull(target.tile, "wilderness_muddy_chest_open")?.replace("wilderness_muddy_chest_closed")
    }

    private fun loot(player: Player, table: DropTable) {
        for (drop in table.roll()) {
            val item = drop.toItem()
            player.addOrDrop(item.id, item.amount)
        }
    }
}
