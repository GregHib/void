package rs.dusk.world.activity.bank

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.awaitInterface
import rs.dusk.engine.client.ui.close
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.IntVariable
import rs.dusk.engine.client.variable.Variable
import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.client.variable.setVar
import rs.dusk.engine.entity.character.contain.sendContainer
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.activity.bank.Bank.sendLastDeposit
import rs.dusk.world.command.Command
import rs.dusk.world.interact.entity.player.display.InterfaceOption

IntVariable(4893, Variable.Type.VARBIT).register("open_bank_tab")
IntVariable(4885, Variable.Type.VARBIT, persistent = true).register("bank_tab_1")
IntVariable(4886, Variable.Type.VARBIT, persistent = true).register("bank_tab_2")
IntVariable(4887, Variable.Type.VARBIT, persistent = true).register("bank_tab_3")
IntVariable(4888, Variable.Type.VARBIT, persistent = true).register("bank_tab_4")
IntVariable(4889, Variable.Type.VARBIT, persistent = true).register("bank_tab_5")
IntVariable(4890, Variable.Type.VARBIT, persistent = true).register("bank_tab_6")
IntVariable(4891, Variable.Type.VARBIT, persistent = true).register("bank_tab_7")
IntVariable(4892, Variable.Type.VARBIT, persistent = true).register("bank_tab_8")

Command where { prefix == "bank" } then {
    player.open("bank")
    if(player.bank.isEmpty()) {
        player.bank.add(995, 1000)
        player.bank.add(4151, 1)
        player.bank.add(11694, 1)
        player.sendContainer("bank")
    }
}

InterfaceOpened where { name == "bank" } then {
    player.action(ActionType.Bank) {
        try {
            player.open("bank_side")
            player.setVar("open_bank_tab", 1)
            player.setVar("bank_tab_1", 1)
            sendLastDeposit(player)
            player.interfaceOptions.unlockAll("bank", "container", 0 until 516)
            player.interfaceOptions.unlockAll("bank_side", "container", 0 until 28)
            awaitInterface(name)
        } finally {
            player.close("bank")
            player.open("inventory")
        }
    }
}


InterfaceOption where { name == "bank" && component == "equipment" && option == "Show Equipment Stats" } then {
    player.open("equipment_bonuses")
    player.setVar("equipment_banking", true)
}

InterfaceOption where { name == "equipment_bonuses" && component == "bank" && option == "Show bank" && player.getVar("equipment_banking", false) } then {
    player.open("bank")
}