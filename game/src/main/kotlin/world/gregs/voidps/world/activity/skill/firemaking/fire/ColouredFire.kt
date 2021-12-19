package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.ColouredLog

enum class ColouredFire(
    override val log: ColouredLog,
    override val level: Int,
    override val xp: Double,
    override val life: Int
) : Fire {
    Red(ColouredLog.RedLogs, 1, 50.0, 60),
    Green(ColouredLog.GreenLogs, 1, 50.0, 60),
    Blue(ColouredLog.BlueLogs, 1, 50.0, 60),
    White(ColouredLog.WhiteLogs, 1, 50.0, 60),
    Purple(ColouredLog.PurpleLogs, 1, 50.0, 60);

    override val id: String = name.toUnderscoreCase()

    override val chance: IntRange = 256..256
}