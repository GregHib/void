package content.entity.player.modal

import net.pearx.kasechange.toSnakeCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.instruction.onInstruction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.client.instruction.ChangeDisplayMode

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
    "task_popup",
    "stats",
    "quest_journals",
    "inventory",
    "worn_equipment",
    "prayer_list",
    "modern_spellbook",
    "friends_list",
    "ignore_list",
    "clan_chat",
    "options",
    "emotes",
    "music_player",
    "notes",
    "area_status_icon"
)

Tab.entries.forEach { tab ->
    val name = tab.name.toSnakeCase()
    interfaceOption(name.toTitleCase(), name, "toplevel") {
        player["tab", false] = tab.name
    }
    interfaceOption(name.toTitleCase(), name, "toplevel_full") {
        player["tab", false] = tab.name
    }
}

onInstruction<ChangeDisplayMode> { player ->
    if (player.interfaces.displayMode == displayMode || !player.hasOpen("graphics_options")) {
        return@onInstruction
    }
    player.interfaces.setDisplayMode(displayMode)
}

interfaceOpen("toplevel") { player ->
    openGamframe(player)
}

interfaceOpen("toplevel_full") { player ->
    openGamframe(player)
}

interfaceRefresh("toplevel", "dialogue_npc*") { player ->
    player.interfaces.sendVisibility(player.interfaces.gameFrame, "wilderness_level", false)
    player.weakQueue("wild_level", 1, onCancel = null) {
        player.interfaces.sendVisibility(player.interfaces.gameFrame, "wilderness_level", false)
    }
}

interfaceRefresh("toplevel_full", "dialogue_npc*") { player ->
    player.interfaces.sendVisibility(player.interfaces.gameFrame, "wilderness_level", false)
    player.weakQueue("wild_level", 1, onCancel = null) {
        player.interfaces.sendVisibility(player.interfaces.gameFrame, "wilderness_level", false)
    }
}

fun GameFrame.openGamframe(player: Player) {
    for (name in list) {
        if (name.endsWith("_spellbook")) {
            val book = player["spellbook_config", 0] and 0x3
            player.open(
                when (book) {
                    1 -> "ancient_spellbook"
                    2 -> "lunar_spellbook"
                    3 -> "dungeoneering_spellbook"
                    else -> name
                }
            )
        } else {
            player.open(name)
        }
    }
}