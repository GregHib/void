package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.world.activity.bank.Bank.tabs

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
    player.close("bank_side")
    player.sendScript("clear_dialogues")
}

interfaceOpen("bank") { player ->
    player.sendInventory("bank")
    player.open("bank_side")
    player.sendVariable("open_bank_tab")
    player.sendVariable("bank_item_mode")
    for (tab in tabs) {
        player.sendVariable("bank_tab_$tab")
    }
    player.sendVariable("last_bank_amount")
    player.sendScript("update_bank_slots")
    player.interfaceOptions.unlockAll("bank", "inventory", 0 until 516)
    player.interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
}

interfaceOption("Show Equipment Stats", "equipment", "bank") {
    player.open("equipment_bonuses")
//    player.setVar("equipment_banking", true)
}

interfaceOption("Show bank", "bank", "equipment_bonuses") {
    if (player["equipment_banking", false]) {
        player.open("bank")
    }
}