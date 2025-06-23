package world.gregs.voidps.cache.definition.data

enum class QuickChatType(var id: Int, var byteCount: Int, var length: Int) {
    MultipleChoice(0, 2, 1),
    AllItems(1, 2, 0),
    Unused(2, 4, 0),
    SkillLevel(4, 1, 1),
    SlayerAssignment(6, 4, 2),
    ClanRank(7, 0, 1),
    Varp(8, 4, 1),
    Varbit(9, 4, 1),
    TradeItems(10, 2, 0),
    SkillExperience(11, 0, 2),
    Unused2(12, 0, 0),
    AverageCombatLevel(13, 0, 0),
    SoulWars(14, 4, 1),
    CombatLevel(15, 0, 0),
    ;

    companion object {
        fun getType(id: Int): QuickChatType? = entries.firstOrNull { it.id == id }
    }
}
