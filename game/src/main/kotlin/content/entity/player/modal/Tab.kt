package content.entity.player.modal

import world.gregs.voidps.engine.entity.character.player.Player

enum class Tab {
    CombatStyles,
    TaskSystem,
    Stats,
    QuestJournals,
    Inventory,
    WornEquipment,
    PrayerList,
    MagicSpellbook,
    FriendsList,
    FriendsChat,
    ClanChat,
    Options,
    Emotes,
    MusicPlayer,
    Notes;
}

fun Player.tab(tab: Tab) {
    this["tab"] = tab.name
}