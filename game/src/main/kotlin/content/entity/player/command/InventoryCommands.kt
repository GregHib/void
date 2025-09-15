package content.entity.player.command

import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory

@Script
class InventoryCommands {
    init {
        modCommand("clear", desc = "Delete all items in the players inventory") { player, args ->
            player.inventory.clear()
        }
    }
}
