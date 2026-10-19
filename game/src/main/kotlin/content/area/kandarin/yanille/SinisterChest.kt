package content.area.kandarin.yanille

import content.entity.effect.toxin.poison
import content.entity.gfx.areaGfx
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class SinisterChest(private val dropTables: DropTables) : Script {

    init {
        objectOperate("Open", "sinister_chest_closed") { (target) ->
            openAndLoot(this, target)
        }

        itemOnObjectOperate("sinister_key", "sinister_chest_closed") { (target) ->
            openAndLoot(this, target)
        }
    }

    private suspend fun openAndLoot(player: Player, target: GameObject) {
        val table = dropTables.get("sinister_chest_drop_table") ?: return
        if (!player.inventory.contains("sinister_key")) {
            player.sound("locked")
            player.message("This chest is locked.")
            return
        }
        if (!player.inventory.remove("sinister_key")) {
            return
        }
        player.anim("open_chest")
        player.sound("unlock")
        player.message("You unlock the chest with your key...")
        player.delay(1)
        player.message("A foul gas seeps from the chest")
        player.sound("chest_open")
        areaGfx("random_event_puff", player.tile)
        target.replace("opened_chest_barbarian_village", ticks = 4)
        loot(player, table)
        player.message("You find a lot of herbs in the chest.")
        player.poison(player, 62)
    }

    private fun loot(player: Player, table: DropTable) {
        for (drop in table.roll()) {
            val item = drop.toItem()
            player.addOrDrop(item.id, item.amount)
        }
    }
}
