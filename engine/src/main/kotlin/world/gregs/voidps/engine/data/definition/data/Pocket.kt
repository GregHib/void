package world.gregs.voidps.engine.data.definition.data

/**
 * @param level required to pickpocket
 * @param xp experience per pickpocket
 * @param stunHit the amount of damage when caught
 * @param stunTicks the amount of ticks to stun for
 * @param chance the chance of being successful
 * @param caughtMessage npc message when caught
 */
data class Pocket(
    val level: Int = 1,
    val xp: Double = 0.0,
    val stunHit: Int = 0,
    val stunTicks: Int = 1,
    val chance: IntRange = 1..1,
    val caughtMessage: String = "What do you think you're doing?"
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Pocket(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            stunHit = map["stun_hit"] as? Int ?: EMPTY.stunHit,
            stunTicks = map["stun_ticks"] as? Int ?: EMPTY.stunTicks,
            chance = map["chance"] as? IntRange ?: EMPTY.chance,
            caughtMessage = map["caught"] as? String ?: EMPTY.caughtMessage,
        )

        val EMPTY = Pocket()
    }
}