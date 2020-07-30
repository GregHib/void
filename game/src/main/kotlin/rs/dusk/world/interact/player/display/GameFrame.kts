package rs.dusk.world.interact.player.display

import rs.dusk.engine.action.Suspension
import rs.dusk.engine.client.ui.Tab
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.character.player.PlayerRegistered
import rs.dusk.engine.model.entity.character.set

PlayerRegistered then {
    player.open(player.gameFrame.name)

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
        "friends_chat",
        "clan_chat",
        "options",
        "emotes",
        "music_player",
        "notes",
        "area_status_icon"
    )
    list.forEach {
        player.open(it)
    }
}

fun String.toUnderscoreCase(): String {
    val builder = StringBuilder()
    for(i in 0 until length) {
        val char = this[i]
        if(char.isUpperCase()) {
            if(i != 0) {
                builder.append('_')
            }
            builder.append(char.toLowerCase())
        }
    }
    return builder.toString()
}

Tab.values().forEach { tab ->
    val name = tab.name.toUnderscoreCase()
    on(InterfaceInteraction) {
        where {
            name == player.gameFrame.name && component == name && option == name
        }
        then {
            player["tab"] = tab
        }
    }
}

InterfaceClosed where { (player.action.suspension as? Suspension.Interface)?.id == id } then {
    player.action.resume()
}