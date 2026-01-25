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
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.sendInventory

class BankOpen(val accounts: AccountDefinitions) : Script {

    init {
        adminCommand("bank", stringArg("player-name", optional = true, autofill = accounts.displayNames.keys), desc = "Open the players bank", handler = ::bank)

        objectOperate("Use-quickly") {
            open("bank")
        }

        objectOperate("Collect") {
            open("collection_box")
        }

        interfaceClosed("bank") {
            set("bank_hidden", true)
            close("bank_side")
            open("inventory")
            sendScript("clear_dialogues")
        }

        interfaceOpened("bank_deposit_box") {
            tab(Tab.Inventory)
            open("bank_side")
            interfaceOptions.send("bank_deposit_box", "inventory")
            interfaceOptions.unlockAll("bank_deposit_box", "inventory", 0 until 28)
            interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
        }

        interfaceClosed("bank_deposit_box") {
            close("bank_side")
            sendScript("clear_dialogues")
        }

        interfaceOpened("bank") {
            set("bank_hidden", false)
            sendInventory("bank")
            open("bank_side")
            sendVariable("open_bank_tab")
            sendVariable("bank_item_mode")
            sendVariable("bank_notes")
            for (tab in tabs) {
                sendVariable("bank_tab_$tab")
            }
            sendVariable("last_bank_amount")
            sendScript("update_bank_slots")
            set("bank_search_reset", true)
            interfaceOptions.unlockAll("bank", "inventory", 0 until 516)
            interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
            tab(Tab.Inventory)
        }

        interfaceOption("Show Equipment Stats", "bank:equipment") {
            set("equipment_bank_button", true)
            set("bank_hidden", true)
            open("equipment_bonuses")
        }

        interfaceOption("Show bank", "equipment_bonuses:bank") {
            if (get("equipment_bank_button", false)) {
                set("bank_hidden", false)
                open("bank")
            }
        }

        objectOperate("Deposit", "bank_deposit_box*") {
            open("bank_deposit_box")
        }
    }

    fun bank(player: Player, args: List<String>) {
        val target = Players.find(player, args.getOrNull(0)) ?: return
        if (target != player) {
            player.message("${Colours.RED_ORANGE.toTag()}Note: modifications won't effect target players bank!")
        }
        player.open("bank")
        player.sendInventory(target.bank)
    }
}
