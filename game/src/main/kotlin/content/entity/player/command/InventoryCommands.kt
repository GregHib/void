package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.sendInventory

class InventoryCommands(val accounts: AccountDefinitions) : Script {

    init {
        modCommand("clear", desc = "Delete all items in the players inventory") {
            inventory.clear()
        }

        adminCommand("inv", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Open the players inventory", handler = ::inv)
    }

    fun inv(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        if (target != player) {
            player.message("${Colours.RED_ORANGE.toTag()}Note: modifications won't effect target players inventory!")
        }
        player.sendInventory(target.inventory)
    }
}
