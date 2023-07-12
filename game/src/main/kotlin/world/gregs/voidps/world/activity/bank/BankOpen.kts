package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.tabs

on<Command>({ prefix == "bank" }) { player: Player ->
    player.open("bank")
}

on<ObjectOption>({ operate && option == "Use-quickly" }) { player: Player ->
    player.open("bank")
}

on<ObjectOption>({ operate && option == "Collect" }) { player: Player ->
    player.open("collection_box")
}

on<InterfaceClosed>({ id == "bank" }) { player: Player ->
    player.close("bank_side")
}

on<InterfaceOpened>({ id == "bank" }) { player: Player ->
    player.sendContainer("bank")
    player.open("bank_side")
    player.sendVariable("open_bank_tab")
    player.sendVariable("bank_item_mode")
    for (tab in tabs) {
        player.sendVariable("bank_tab_$tab")
    }
    player.sendVariable("last_bank_amount")
    player.sendScript(1465)
    player.interfaceOptions.unlockAll("bank", "container", 0 until 516)
    player.interfaceOptions.unlockAll("bank_side", "container", 0 until 28)
}

on<InterfaceOption>({ id == "bank" && component == "equipment" && option == "Show Equipment Stats" }) { player: Player ->
    player.open("equipment_bonuses")
//    player.setVar("equipment_banking", true)
}

on<InterfaceOption>({ id == "equipment_bonuses" && component == "bank" && option == "Show bank" && it["equipment_banking", false] }) { player: Player ->
    player.open("bank")
}