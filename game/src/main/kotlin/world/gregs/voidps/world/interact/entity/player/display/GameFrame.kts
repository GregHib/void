package world.gregs.voidps.world.interact.entity.player.display

import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.ListVariable
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.login.PlayerRegistered
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

ListVariable(168, Variable.Type.VARC, values = Tab.values().toList(), defaultValue = Tab.Inventory).register("tab")

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
    "friends_chat",
    "options",
    "emotes",
    "music_player",
    "notes",
    "area_status_icon"
)

PlayerRegistered then {
    player.open(player.gameFrame.name)

    list.forEach {
        player.open(it)
    }
}

fun String.toUnderscoreCase(): String {
    val builder = StringBuilder()
    for (i in 0 until length) {
        val char = this[i]
        if (char.isUpperCase()) {
            if (i != 0) {
                builder.append('_')
            }
            builder.append(char.toLowerCase())
        }
    }
    return builder.toString()
}

Tab.values().forEach { tab ->
    val name = tab.name.toUnderscoreCase()
    InterfaceOption where { name == player.gameFrame.name && component == name && option == name } then {
        player.setVar("tab", tab, refresh = false)
    }
}

InterfaceOpened where { name == player.gameFrame.name } then {
    // Remove immediately on Login
    delay { player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false) }
    // Screen change needs an extra tick delay for some unknown reason
    delay(1) { player.interfaces.sendVisibility(player.gameFrame.name, "wilderness_level", false) }
}

InterfaceClosed where { (player.action.suspension as? Suspension.Interface)?.id == name } then {
    player.action.resume()
}