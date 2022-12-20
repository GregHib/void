package world.gregs.voidps.world.activity.bank

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.awaitInterface
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.sendContainer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.Bank.tabs

on<Command>({ prefix == "bank" }) { player: Player ->
    player.open("bank")
    if (player.bank.isEmpty()) {
        player.bank.apply {
            add("red_partyhat", 1)
            add("yellow_partyhat", 1)
            add("blue_partyhat", 1)
            add("green_partyhat", 1)
            add("purple_partyhat", 1)
            add("white_partyhat", 1)
            add("coins", 1000)
            add("abyssal_whip", 1)
            add("armadyl_godsword", 1)
        }
    }
}

on<ObjectOption>({ option == "Use-quickly" }) { player: Player ->
    player.open("bank")
}

on<InterfaceOpened>({ id == "bank" }) { player: Player ->
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
            awaitInterface(id)
        } finally {
            player.close("bank_side")
            player.close("bank")
        }
    }
}

on<InterfaceOption>({ id == "bank" && component == "equipment" && option == "Show Equipment Stats" }) { player: Player ->
    player.open("equipment_bonuses")
//    player.setVar("equipment_banking", true)
}

on<InterfaceOption>({ id == "equipment_bonuses" && component == "bank" && option == "Show bank" && it.getVar("equipment_banking", false) }) { player: Player ->
    player.open("bank")
}