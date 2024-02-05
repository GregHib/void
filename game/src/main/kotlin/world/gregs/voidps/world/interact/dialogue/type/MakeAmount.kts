package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOption({ id == "skill_creation_amount" && component == "create1" }) { player: Player ->
    player["skill_creation_amount", false] = 1
}

interfaceOption({ id == "skill_creation_amount" && component == "create5" }) { player: Player ->
    player["skill_creation_amount", false] = 5
}

interfaceOption({ id == "skill_creation_amount" && component == "create10" }) { player: Player ->
    player["skill_creation_amount", false] = 10
}

interfaceOption({ id == "skill_creation_amount" && component == "all" }) { player: Player ->
    val max: Int = player["skill_creation_maximum", 1]
    player["skill_creation_amount", false] = max
}

interfaceOption({ id == "skill_creation_amount" && component == "increment" }) { player: Player ->
    var current: Int = player["skill_creation_amount", 1]
    val maximum: Int = player["skill_creation_maximum", 1]
    current++
    if (current > maximum) {
        current = maximum
    }
    player["skill_creation_amount"] = current
}

interfaceOption({ id == "skill_creation_amount" && component == "decrement" }) { player: Player ->
    var current: Int = player["skill_creation_amount", 1]
    current--
    if (current < 0) {
        current = 0
    }
    player["skill_creation_amount"] = current
}