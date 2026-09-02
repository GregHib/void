package content.entity.player.modal

import content.area.misthalin.tutorial_island.tutorialUnlocked
import net.pearx.kasechange.toSnakeCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.client.instruction.ChangeDisplayMode

class GameFrame : Script {

    val list = gameFrameComponents

    init {
        Tab.entries.forEach { tab ->
            val name = tab.name.toSnakeCase()
            interfaceOption(name.toTitleCase(), "toplevel*:$name") {
                set("tab", false, tab.name)
            }
        }

        instruction<ChangeDisplayMode> { player ->
            if (player.interfaces.displayMode == displayMode || !player.hasOpen("graphics_options")) {
                return@instruction
            }
            player.interfaces.setDisplayMode(displayMode)
        }

        playerSpawn {
            softTimers.start("gameframe_login_refresh")
        }

        timerStart("gameframe_login_refresh") { 2 }

        timerTick("gameframe_login_refresh") {
            // Reload chat_background to reveal display name
            interfaces.refresh("chat_background")
            Timer.CANCEL
        }

        interfaceOpened("toplevel*") {
            openGamframe(this)
        }

        interfaceRefresh("toplevel*,dialogue_npc*") {
            interfaces.sendVisibility(interfaces.gameFrame, "wilderness_level", false)
            softTimers.start("wilderness_level_refresh")
        }

        timerStart("wilderness_level_refresh") { 1 }

        timerStop("wilderness_level_refresh") {
            interfaces.sendVisibility(interfaces.gameFrame, "wilderness_level", false)
        }
    }

    fun GameFrame.openGamframe(player: Player) {
        for (name in list) {
            if (!player.tutorialUnlocked(if (name.endsWith("_spellbook")) "modern_spellbook" else name)) {
                player.interfaces.sendVisibility(player.interfaces.gameFrame, tabComponent(name), false)
                continue
            }
            if (name.endsWith("_spellbook")) {
                val book = player["spellbook_config", 0] and 0x3
                player.open(
                    when (book) {
                        1 -> "ancient_spellbook"
                        2 -> "lunar_spellbook"
                        3 -> "dungeoneering_spellbook"
                        else -> name
                    },
                )
            } else {
                player.open(name)
            }
        }
    }
}

/**
 * The toplevel component holding a game frame entry's tab button. Every spellbook opens into
 * the one `magic_spellbook` button.
 */
fun tabComponent(name: String): String = if (name.endsWith("_spellbook")) "magic_spellbook" else name

/** Every game frame component opened on login, in order. */
val gameFrameComponents = listOf(
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
    "area_status_icon",
)
