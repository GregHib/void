package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toInt

on<InterfaceOpened>({ name.endsWith("_spellbook") }) { player: Player ->
    val id = when (name) {
        "ancient_spellbook" -> 1
        "lunar_spellbook" -> 2
        "dungeoneering_spellbook" -> 3
        else -> 0
    }
    player.setVar("spellbook_config", id or (player.getVar("defensive_cast", false).toInt() shl 8))
}

on<Registered> { player: Player ->
    player.sendVar("spellbook_sort")
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component.startsWith("filter_") }) { player: Player ->
    val key = "spellbook_sort"
    val id = "${name}_$component"
    if (player.hasVar(key, id)) {
        player.removeVar(key, id)
    } else {
        player.addVar(key, id)
    }
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component.startsWith("sort_") }) { player: Player ->
    val key = "spellbook_sort"
    if (component.startsWith("sort_")) {
        // Make sure don't sort by multiple at once
        player.removeVar(key, "${name}_sort_combat", refresh = false)
        player.removeVar(key, "${name}_sort_teleport", refresh = false)
    }
    if (component != "sort_level") {
        player.addVar(key, "${name}_$component", refresh = false)
    }
}

on<InterfaceOption>({ name.endsWith("_spellbook") && component == "defensive_cast" && option == "Defensive Casting" }) { player: Player ->
    player.toggleVar(component)
}