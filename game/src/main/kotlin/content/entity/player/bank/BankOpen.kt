package content.entity.player.bank

import content.entity.player.bank.Bank.tabs
import content.entity.player.command.find
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.sendInventory

class BankOpen : Script {

    val players: Players by inject()
    val accounts: AccountDefinitions by inject()

    init {
        adminCommand("bank", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Open the players bank", handler = ::bank)

        objectOperate("Use-quickly") {
            open("bank")
        }

        objectOperate("Collect") {
            open("collection_box")
        }

        interfaceClose("bank") { player ->
            player["bank_hidden"] = true
            player.close("bank_side")
            player.sendScript("clear_dialogues")
        }

        interfaceOpen("bank_deposit_box") { player ->
            player.tab(Tab.Inventory)
            player.open("bank_side")
            player.interfaceOptions.send("bank_deposit_box", "inventory")
            player.interfaceOptions.unlockAll("bank_deposit_box", "inventory", 0 until 28)
            player.interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
        }

        interfaceClose("bank_deposit_box") { player ->
            player.close("bank_side")
            player.sendScript("clear_dialogues")
        }

        interfaceOpen("bank") { player ->
            player["bank_hidden"] = false
            player.sendInventory("bank")
            player.open("bank_side")
            player.sendVariable("open_bank_tab")
            player.sendVariable("bank_item_mode")
            player.sendVariable("bank_notes")
            for (tab in tabs) {
                player.sendVariable("bank_tab_$tab")
            }
            player.sendVariable("last_bank_amount")
            player.sendScript("update_bank_slots")
            player["bank_search_reset"] = true
            player.interfaceOptions.unlockAll("bank", "inventory", 0 until 516)
            player.interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
            player.tab(Tab.Inventory)
        }

        interfaceOption("Show Equipment Stats", "equipment", "bank") {
            player["equipment_bank_button"] = true
            player["bank_hidden"] = true
            player.open("equipment_bonuses")
        }

        interfaceOption("Show bank", "bank", "equipment_bonuses") {
            if (player["equipment_bank_button", false]) {
                player["bank_hidden"] = false
                player.open("bank")
            }
        }

        objectOperate("Deposit", "bank_deposit_box") {
            open("bank_deposit_box")
        }
    }

    fun bank(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        if (target != player) {
            player.message("${Colours.RED_ORANGE.toTag()}Note: modifications won't effect target players bank!")
        }
        player.open("bank")
        player.sendInventory(target.bank)
    }
}
