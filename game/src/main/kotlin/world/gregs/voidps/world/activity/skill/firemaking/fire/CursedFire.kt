package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.engine.utility.toUnderscoreCase
import world.gregs.voidps.world.activity.skill.woodcutting.log.CursedLog

enum class CursedFire(
    override val log: CursedLog,
    override val level: Int,
    override val xp: Double,
    override val life: Int
) : Fire {
    CursedMagic(CursedLog.CursedMagicLogs, 82, 303.8, 200);

    override val id: String = name.toUnderscoreCase()

}