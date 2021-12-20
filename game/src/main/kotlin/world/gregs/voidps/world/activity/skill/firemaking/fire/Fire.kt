package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.woodcutting.log.Log

interface Fire : Id {
    val log: Log
    val level: Int
    val xp: Double
    val life: Int
    val chance: IntRange

    companion object {

        private val fires: Array<Fire> = arrayOf(
            *RegularFire.values(),
            *ColouredFire.values()
        )

        fun get(id: String): Fire? {
            return fires.firstOrNull { fire -> fire.log.id == id }
        }
    }
}