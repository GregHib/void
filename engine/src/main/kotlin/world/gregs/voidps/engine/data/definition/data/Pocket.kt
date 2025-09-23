package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

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
    val stunHit: IntRange = 0..0,
    val stunTicks: Int = 1,
    val chance: IntRange = 1..1,
    val caughtMessage: String = "What do you think you're doing?",
    val table: String? = null,
) {
    companion object {

        operator fun invoke(reader: ConfigReader): Pocket {
            var level = 1
            var xp = 0.0
            var stunHit = 0
            var stunHitMin = 0
            var stunHitMax = 0
            var stunTicks = 1
            var chanceMin = 255
            var chanceMax = 255
            var caughtMessage = "What do you think you're doing?"
            var table: String? = null
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "stun_hit" -> stunHit = reader.int()
                    "stun_hit_min" -> stunHitMin = reader.int()
                    "stun_hit_max" -> stunHitMax = reader.int()
                    "stun_ticks" -> stunTicks = reader.int()
                    "chance_min" -> chanceMin = reader.int()
                    "chance_max" -> chanceMax = reader.int()
                    "caught_message" -> caughtMessage = reader.string()
                    "table" -> table = reader.string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Pocket(
                level = level,
                xp = xp,
                stunHit = if (stunHit != 0) stunHit..stunHit else stunHitMin..stunHitMax,
                stunTicks = stunTicks,
                chance = chanceMin until chanceMax,
                caughtMessage = caughtMessage,
                table = table
            )
        }

        val EMPTY = Pocket()
    }
}
