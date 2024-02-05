package world.gregs.voidps.world.interact.entity.player.display

import net.pearx.kasechange.toSnakeCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.queue.weakQueue

val list = listOf(
    "chat_box",
    "chat_background",
    "filter_buttons",
    "private_chat",
    "health_orb",
    "prayer_orb",
    "energy_orb",
    "summoning_orb",
    "combat_styles",
    "task_system",
    "stats",
    "quest_journals",
    "inventory",
    "worn_equipment",
    "prayer_list",
    "modern_spellbook",
    "friends_list",
    "ignores_list",
    "clan_chat",
    "options",
    "emotes",
    "music_player",
    "notes",
    "area_status_icon"
)

playerSpawn { player: Player ->
    player.open(player.gameFrame.name)
}

Tab.entries.forEach { tab ->
    val name = tab.name.toSnakeCase()
    interfaceOption({ id == it.gameFrame.name && component == name && option == name.toTitleCase() }) { player: Player ->
        player["tab", false] = tab.name
    }
}

interfaceOpen("toplevel*") { player: Player ->
    list.forEach { name ->
        if (name.endsWith("_spellbook")) {
            val book = player["spellbook_config", 0] and 0x3
            player.open(when (book) {
                1 -> "ancient_spellbook"
                2 -> "lunar_spellbook"
                3 -> "dungeoneering_spellbook"
                else -> name
            })
        } else {
            player.open(name)
        }
    }
}

interfaceRefresh("toplevel*", "dialogue_npc*") { player: Player ->
    player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false)
    player.weakQueue("wild_level", 1) {
        player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false)
    }
}