package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book

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
}

interfaceOption(component = "filter_*", id = "*_spellbook") {
    val key = "spellbook_sort"
    val id = "${id}_$component"
    if (player.containsVarbit(key, id)) {
        player.removeVarbit(key, id)
    } else {
        player.addVarbit(key, id)
    }
}

interfaceOption(component = "sort_*", id = "*_spellbook") {
    val key = "spellbook_sort"
    if (component.startsWith("sort_")) {
        // Make sure don't sort by multiple at once
        player.removeVarbit(key, "${id}_sort_combat", refresh = false)
        player.removeVarbit(key, "${id}_sort_teleport", refresh = false)
    }
    if (component != "sort_level") {
        player.addVarbit(key, "${id}_$component", refresh = false)
    }
}

interfaceOption("Defensive Casting", "defensive_cast", "*_spellbook") {
    player.toggle(component)
}