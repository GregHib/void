package content.entity.player.bank

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.sendInventory
import content.entity.player.bank.Bank.tabs
import content.entity.player.modal.Tab
import content.entity.player.modal.tab

adminCommand("bank", "open your bank anywhere") {
    player.open("bank")
}

objectOperate("Use-quickly") {
    player.open("bank")
}

objectOperate("Collect") {
    player.open("collection_box")
}

interfaceClose("bank") { player ->
    player["bank_hidden"] = true
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