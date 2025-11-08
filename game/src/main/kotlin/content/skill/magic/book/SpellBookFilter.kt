package content.skill.magic.book

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.player.Player

class SpellBookFilter : Script {

    init {
        playerSpawn {
            sendVariable("spellbook_sort")
            sendVariable("spellbook_config")
        }

        interfaceOpened("*_spellbook") { id ->
            val id = when (id) {
                "ancient_spellbook" -> 1
                "lunar_spellbook" -> 2
                "dungeoneering_spellbook" -> 3
                else -> 0
            }
            set("spellbook_config", id or (get("defensive_cast", false).toInt() shl 8))
        }

        interfaceOption(id = "*_spellbook:filter_*") {
            filter(it.id)
        }

        interfaceOption(id = "*_spellbook:sort_*") {
            sort(it.id, it.component)
        }

        interfaceOption("Defensive Casting", "*_spellbook:defensive_cast") {
            toggle(it.component)
        }
    }

    fun Player.filter(id: String) {
        val key = "spellbook_sort"
        if (containsVarbit(key, id)) {
            removeVarbit(key, id)
        } else {
            addVarbit(key, id)
        }
    }

    fun Player.sort(id: String, component: String) {
        val key = "spellbook_sort"
        if (component.startsWith("sort_")) {
            // Make sure don't sort by multiple at once
            removeVarbit(key, "${id}_sort_combat", refresh = false)
            removeVarbit(key, "${id}_sort_teleport", refresh = false)
        }
        if (component != "sort_level") {
            addVarbit(key, "$id:$component", refresh = false)
        }
    }
}
