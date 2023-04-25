package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create1" }) { player: Player ->
    player["skill_creation_amount", false] = 1
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create5" }) { player: Player ->
    player["skill_creation_amount", false] = 5
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "create10" }) { player: Player ->
    player["skill_creation_amount", false] = 10
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "all" }) { player: Player ->
    val max: Int = player["skill_creation_maximum"]
    player["skill_creation_amount", false] = max
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "increment" }) { player: Player ->
    var current: Int = player["skill_creation_amount"]
    val maximum: Int = player["skill_creation_maximum"]
    current++
    if (current > maximum) {
        current = maximum
    }
    player["skill_creation_amount"] = current
}

on<InterfaceOption>({ id == "skill_creation_amount" && component == "decrement" }) { player: Player ->
    var current: Int = player["skill_creation_amount"]
    current--
    if (current < 0) {
        current = 0
    }
    player["skill_creation_amount"] = current
}