package rs.dusk.engine.client.ui

import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.character.player.PlayerRegistered

PlayerRegistered then {
    player.open(player.gameframe.name)

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