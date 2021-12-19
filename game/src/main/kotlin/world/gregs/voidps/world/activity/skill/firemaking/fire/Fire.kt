package world.gregs.voidps.world.activity.skill.firemaking.fire

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.woodcutting.log.Log

interface Fire : Id {
    val log: Log
    val level: Int
    val xp: Double
    val life: Int

    companion object {

        private val fires: Array<Fire> = arrayOf(
            *RegularFire.values(),
            *ColouredFire.values()
        )

        fun get(item: Item): Fire? {
            return fires.firstOrNull { fire -> fire.log.id == item.id }
        }
    }
}