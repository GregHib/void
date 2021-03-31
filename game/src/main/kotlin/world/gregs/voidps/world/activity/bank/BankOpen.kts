package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.awaitInterface
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.IntVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.sendScript
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.activity.bank.Bank.tabs

IntVariable(4893, Variable.Type.VARBIT, persistent = true, defaultValue = 1).register("open_bank_tab")
IntVariable(4885, Variable.Type.VARBIT, persistent = true).register("bank_tab_1")
IntVariable(4886, Variable.Type.VARBIT, persistent = true).register("bank_tab_2")
IntVariable(4887, Variable.Type.VARBIT, persistent = true).register("bank_tab_3")
IntVariable(4888, Variable.Type.VARBIT, persistent = true).register("bank_tab_4")
IntVariable(4889, Variable.Type.VARBIT, persistent = true).register("bank_tab_5")
IntVariable(4890, Variable.Type.VARBIT, persistent = true).register("bank_tab_6")
IntVariable(4891, Variable.Type.VARBIT, persistent = true).register("bank_tab_7")
IntVariable(4892, Variable.Type.VARBIT, persistent = true).register("bank_tab_8")

on<Command>({ prefix == "bank" }) { player: Player ->
    player.open("bank")
    if (player.bank.isEmpty()) {
        for (i in 1038 until 1048 step 2) {
            player.bank.add(i, 1)
        }
        player.bank.add(995, 1000)
        player.bank.add(4151, 1)
        player.bank.add(11694, 1)
    }
}

on<ObjectOption>({ option == "Use-quickly" }) { player: Player ->
    player.open("bank")
}

on<InterfaceOpened>({ name == "bank" }) { player: Player ->
    player.action(ActionType.Bank) {
        try {
            player.sendContainer("bank")
            player.open("bank_side")
            player.sendVar("open_bank_tab")
            player.sendVar("bank_item_mode")
            for (tab in tabs) {
                player.sendVar("bank_tab_$tab")
            }
            player.sendVar("last_bank_amount")
            player.sendScript(1465)
            player.interfaceOptions.unlockAll("bank", "container", 0 until 516)
            player.interfaceOptions.unlockAll("bank_side", "container", 0 until 28)
            awaitInterface(name)
        } finally {
            player.open("inventory")
            player.close("bank")
        }
    }
}

on<InterfaceOption>({ name == "bank" && component == "equipment" && option == "Show Equipment Stats" }) { player: Player ->
    player.open("equipment_bonuses")
//    player.setVar("equipment_banking", true)
}

on<InterfaceOption>({ name == "equipment_bonuses" && component == "bank" && option == "Show bank" && it.getVar("equipment_banking", false) }) { player: Player ->
    player.open("bank")
}