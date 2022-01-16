package world.gregs.voidps.cache.definition.data


enum class QuickChatType(var id: Int, var bitCount: Int, var length: Int) {
    MultipleChoice(0, 1, 1),
    AllItems(1, 1, 0),
    Unused(2, 3, 0),
    SkillLevel(4, 0, 1),
    SlayerAssignment(6, 3, 2),
    ClanRank(7, 0, 1),
    Varp(8, 3, 1),
    Varbit(9, 3, 1),
    TradeItems(10, 1, 0),
    SkillExperience(11, 0, 2),
    Unused2(12, 0, 0),
    AverageCombatLevel(13, 0, 0),
    SoulWars(14, 3, 1),
    CombatLevel(15, 0, 0);

    companion object {
        private val types = values()

        fun getType(id: Int): QuickChatType? {
            return types.firstOrNull { it.id == id }
        }
    }
}