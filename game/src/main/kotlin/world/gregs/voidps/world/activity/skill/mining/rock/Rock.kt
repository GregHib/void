package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.mining.ore.Ore

interface Rock : Id {
    val ores: List<Ore>
    val level: Int
    val respawnDelay: Int

    companion object {

        private val rocks: Array<Rock> = arrayOf(
            *RegularRock.values(),
            GraniteRock,
            SandstoneRock
        )

        fun get(gameObject: GameObject): Rock? {
            val id = gameObject.stringId
            return rocks.firstOrNull { rock -> id.startsWith(rock.id, true) }
        }
    }
}