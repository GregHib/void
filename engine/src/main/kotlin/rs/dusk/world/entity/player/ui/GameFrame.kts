package rs.dusk.world.entity.player.ui

import rs.dusk.engine.client.ui.Tab
import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.engine.model.entity.character.player.PlayerRegistered
import rs.dusk.engine.model.entity.character.removeValue
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

InterfaceInteraction where { name == player.gameFrame.name && optionId in 39..54 } then {
    val tab = Tab.forId(optionId)
    if(tab == null) {
        player.removeValue("tab")
    } else {
        player["tab"] = tab
    }
}