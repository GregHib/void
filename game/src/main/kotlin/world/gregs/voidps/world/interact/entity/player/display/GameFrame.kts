package world.gregs.voidps.world.interact.entity.player.display

import net.pearx.kasechange.toSnakeCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
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

on<Registered> { player: Player ->
    player.open(player.gameFrame.name)
}

Tab.values().forEach { tab ->
    val name = tab.name.toSnakeCase()
    on<InterfaceOption>({ id == it.gameFrame.name && component == name && option == name.toTitleCase() }) { player: Player ->
        player["tab", false] = tab.name
    }
}

on<InterfaceOpened>({ id == it.gameFrame.name }) { player: Player ->
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

on<InterfaceRefreshed>({ id == it.gameFrame.name || id.startsWith("dialogue_npc") }) { player: Player ->
    player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false)
    player.weakQueue("wild_level", 1) {
        player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false)
    }
}