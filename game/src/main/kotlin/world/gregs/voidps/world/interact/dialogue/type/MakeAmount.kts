package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create1" }) { player: Player ->
    player.setVar("skill_creation_amount", 1, refresh = false)
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create5" }) { player: Player ->
    player.setVar("skill_creation_amount", 5, refresh = false)
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create10" }) { player: Player ->
    player.setVar("skill_creation_amount", 10, refresh = false)
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "all" }) { player: Player ->
    val max = player.getVar("skill_creation_maximum", 1)
    player.setVar("skill_creation_amount", max, refresh = false)
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "increment" }) { player: Player ->
    var current = player.getVar("skill_creation_amount", 0)
    val maximum = player.getVar("skill_creation_maximum", 1)
    current++
    if (current > maximum) {
        current = maximum
    }
    player.setVar("skill_creation_amount", current)
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "decrement" }) { player: Player ->
    var current = player.getVar("skill_creation_amount", 0)
    current--
    if (current < 0) {
        current = 0
    }
    player.setVar("skill_creation_amount", current)
}