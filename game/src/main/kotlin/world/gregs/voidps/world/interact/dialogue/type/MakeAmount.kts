package world.gregs.voidps.world.interact.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption

interfaceOption("skill_creation_amount", "create1") {
    player["skill_creation_amount", false] = 1
}

interfaceOption("skill_creation_amount", "create5") {
    player["skill_creation_amount", false] = 5
}

interfaceOption("skill_creation_amount", "create10") {
    player["skill_creation_amount", false] = 10
}

interfaceOption("skill_creation_amount", "all") {
    val max: Int = player["skill_creation_maximum", 1]
    player["skill_creation_amount", false] = max
}

interfaceOption("skill_creation_amount", "increment") {
    var current: Int = player["skill_creation_amount", 1]
    val maximum: Int = player["skill_creation_maximum", 1]
    current++
    if (current > maximum) {
        current = maximum
    }
    player["skill_creation_amount"] = current
}

interfaceOption("skill_creation_amount", "decrement") {
    var current: Int = player["skill_creation_amount", 1]
    current--
    if (current < 0) {
        current = 0
    }
    player["skill_creation_amount"] = current
}