package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.mining.ore.RegularOre

enum class RegularRock(
    override val ore: RegularOre,
    override val level: Int,
    override val xp: Double
) : Rock {
    Copper(RegularOre.Copper_Ore, 1, 17.5),
    Tin(RegularOre.Tin_Ore, 1, 17.5),
    ;

    override val id: String = name.toLowerCase()

    companion object {
        fun get(gameObject: GameObject): RegularRock? {
            val id = gameObject.stringId
            return values().firstOrNull { id.startsWith(it.name, true) }
        }
    }
}