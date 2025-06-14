package world.gregs.voidps.engine.data.config

/**
 * @param type The slayer monster category
 * @param tip description of the monster and how to slay it
 * @param amount The quantity of monsters assigned to kill
 * @param weight The weighting in the overall list
 * @param slayerLevel Required slayer level
 * @param combatLevel Required combat level
 * @param quests Required quests completed
 */
data class SlayerTaskDefinition(
    val type: String = "",
    val tip: String = "",
    val amount: IntRange = 1..1,
    val weight: Int = 1,
    val slayerLevel: Int = 1,
    val combatLevel: Int = 1,
    val quests: Set<String> = emptySet(),
) {

    companion object {
        val EMPTY = SlayerTaskDefinition()
    }
}
