package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.command
import world.gregs.voidps.engine.client.ui.event.interfaceClosed
import world.gregs.voidps.engine.client.ui.event.interfaceOpened
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.world.activity.bank.Bank.tabs

command({ prefix == "bank" }) { player: Player ->
    player.open("bank")
}

objectOperate({ option == "Use-quickly" }) { player: Player ->
    player.open("bank")
}

objectOperate({ option == "Collect" }) { player: Player ->
    player.open("collection_box")
}

interfaceClosed({ id == "bank" }) { player: Player ->
    player.close("bank_side")
}

interfaceOpened({ id == "bank" }) { player: Player ->
    player.sendInventory("bank")
    player.open("bank_side")
    player.sendVariable("open_bank_tab")
    player.sendVariable("bank_item_mode")
    for (tab in tabs) {
        player.sendVariable("bank_tab_$tab")
    }
    player.sendVariable("last_bank_amount")
    player.sendScript(1465)
    player.interfaceOptions.unlockAll("bank", "inventory", 0 until 516)
    player.interfaceOptions.unlockAll("bank_side", "inventory", 0 until 28)
}

interfaceOption({ id == "bank" && component == "equipment" && option == "Show Equipment Stats" }) { player: Player ->
    player.open("equipment_bonuses")
//    player.setVar("equipment_banking", true)
}

interfaceOption({ id == "equipment_bonuses" && component == "bank" && option == "Show bank" && it["equipment_banking", false] }) { player: Player ->
    player.open("bank")
}