package rs.dusk.engine.client.ui

enum class Tab(val id: Int) {
    CombatStyles(39),
    TaskSystem(40),
    Stats(41),
    QuestJournals(42),
    Inventory(43),
    WornEquipment(44),
    PrayerList(45),
    MagicSpellbook(46),
    FriendsList(48),
    FriendsChat(49),
    ClanChat(50),
    Options(51),
    Emotes(52),
    MusicPlayer(53),
    Notes(54);

    companion object {
        fun forId(id: Int) = values().firstOrNull { it.id == id }
    }
}