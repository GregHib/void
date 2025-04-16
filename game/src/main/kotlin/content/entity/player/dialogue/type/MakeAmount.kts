package content.entity.player.dialogue.type

import world.gregs.voidps.engine.client.ui.interfaceOption

interfaceOption("1", "create1", "skill_creation_amount") {
    player["skill_creation_amount", false] = 1
}

interfaceOption("5", "create5", "skill_creation_amount") {
    player["skill_creation_amount", false] = 5
}

interfaceOption("10", "create10", "skill_creation_amount") {
    player["skill_creation_amount", false] = 10
}

interfaceOption(component = "all", id = "skill_creation_amount") {
    val max: Int = player["skill_creation_maximum", 1]
    player["skill_creation_amount", false] = max
}

interfaceOption("+1", "increment", "skill_creation_amount") {
    var current: Int = player["skill_creation_amount", 1]
    val maximum: Int = player["skill_creation_maximum", 1]
    current++
    if (current > maximum) {
        current = maximum
    }
    player["skill_creation_amount"] = current
}

interfaceOption("-1", "decrement", "skill_creation_amount") {
    var current: Int = player["skill_creation_amount", 1]
    current--
    if (current < 0) {
        current = 0
    }
    player["skill_creation_amount"] = current
}