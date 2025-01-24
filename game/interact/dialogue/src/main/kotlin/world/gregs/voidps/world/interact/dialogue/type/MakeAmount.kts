package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption

interfaceOption(component = "create1", id = "skill_creation_amount") {
    player["skill_creation_amount", false] = 1
}

interfaceOption(component = "create5", id = "skill_creation_amount") {
    player["skill_creation_amount", false] = 5
}

interfaceOption(component = "create10", id = "skill_creation_amount") {
    player["skill_creation_amount", false] = 10
}

interfaceOption(component = "all", id = "skill_creation_amount") {
    val max: Int = player["skill_creation_maximum", 1]
    player["skill_creation_amount", false] = max
}

interfaceOption(component = "increment", id = "skill_creation_amount") {
    var current: Int = player["skill_creation_amount", 1]
    val maximum: Int = player["skill_creation_maximum", 1]
    current++
    if (current > maximum) {
        current = maximum
    }
    player["skill_creation_amount"] = current
}

interfaceOption(component = "decrement", id = "skill_creation_amount") {
    var current: Int = player["skill_creation_amount", 1]
    current--
    if (current < 0) {
        current = 0
    }
    player["skill_creation_amount"] = current
}