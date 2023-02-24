package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id.endsWith("_spellbook") }) { player: Player ->
    val id = when (id) {
        "ancient_spellbook" -> 1
        "lunar_spellbook" -> 2
        "dungeoneering_spellbook" -> 3
        else -> 0
    }
    player.setVar("spellbook_config", id or (player.getVar("defensive_cast", false).toInt() shl 8))
}

on<Registered> { player: Player ->
    player.sendVariable("spellbook_sort")
}

on<InterfaceOption>({ id.endsWith("_spellbook") && component.startsWith("filter_") }) { player: Player ->
    val key = "spellbook_sort"
    val id = "${id}_$component"
    if (player.containsVarbit(key, id)) {
        player.removeVarbit(key, id)
    } else {
        player.addVarbit(key, id)
    }
}

on<InterfaceOption>({ id.endsWith("_spellbook") && component.startsWith("sort_") }) { player: Player ->
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

on<InterfaceOption>({ id.endsWith("_spellbook") && component == "defensive_cast" && option == "Defensive Casting" }) { player: Player ->
    player.toggle(component)
}