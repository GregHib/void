package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.mining.ore.Ore

interface Rock : Id {
    val ore: Ore
    val level: Int
    val xp: Double

    companion object {

        private val rocks: Array<Rock> = arrayOf(
            *RegularRock.values()
        )

        fun get(gameObject: GameObject): Rock? {
            val id = gameObject.stringId
            return rocks.firstOrNull { rock -> id.startsWith("${rock.id}_rocks", true) }
        }
    }
}