package content.skill.magic.book

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.playerSpawn

interfaceOpen("*_spellbook") { player ->
    val id = when (id) {
        "ancient_spellbook" -> 1
        "lunar_spellbook" -> 2
        "dungeoneering_spellbook" -> 3
        else -> 0
    }
    player["spellbook_config"] = id or (player["defensive_cast", false].toInt() shl 8)
}

playerSpawn { player ->
    player.sendVariable("spellbook_sort")
    player.sendVariable("spellbook_config")
}

interfaceOption(component = "filter_*", id = "modern_spellbook") {
    filter()
}

interfaceOption(component = "filter_*", id = "ancient_spellbook") {
    filter()
}

interfaceOption(component = "filter_*", id = "lunar_spellbook") {
    filter()
}

interfaceOption(component = "filter_*", id = "dungeoneering_spellbook") {
    filter()
}

fun InterfaceOption.filter() {
    val key = "spellbook_sort"
    val id = "${id}:$component"
    if (player.containsVarbit(key, id)) {
        player.removeVarbit(key, id)
    } else {
        player.addVarbit(key, id)
    }
}

interfaceOption(component = "sort_*", id = "modern_spellbook") {
    sort()
}

interfaceOption(component = "sort_*", id = "ancient_spellbook") {
    sort()
}

interfaceOption(component = "sort_*", id = "lunar_spellbook") {
    sort()
}

interfaceOption(component = "sort_*", id = "dungeoneering_spellbook") {
    sort()
}

fun InterfaceOption.sort() {
    val key = "spellbook_sort"
    if (component.startsWith("sort_")) {
        // Make sure don't sort by multiple at once
        player.removeVarbit(key, "${id}_sort_combat", refresh = false)
        player.removeVarbit(key, "${id}_sort_teleport", refresh = false)
    }
    if (component != "sort_level") {
        player.addVarbit(key, "${id}:$component", refresh = false)
    }
}

interfaceOption("Defensive Casting", "defensive_cast", "modern_spellbook") {
    player.toggle(component)
}

interfaceOption("Defensive Casting", "defensive_cast", "ancient_spellbook") {
    player.toggle(component)
}

interfaceOption("Defensive Casting", "defensive_cast", "lunar_spellbook") {
    player.toggle(component)
}

interfaceOption("Defensive Casting", "defensive_cast", "dungeoneering_spellbook") {
    player.toggle(component)
}
