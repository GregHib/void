package world.gregs.voidps.engine.data.definition.data

import world.gregs.voidps.engine.utility.toIntRange

/**
 * @param level required to attempt to light
 * @param xp experience from successfully lighting a fire
 * @param chance Chance of creating a fire at level 1 and 99
 * @param life duration in ticks that the fire object will last for
 */
data class Fire(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 65..513,
    val life: Int = 0,
    val colour: String = "orange"
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Fire(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = (map["chance"] as? String)?.toIntRange() ?: EMPTY.chance,
            life = map["life"] as? Int ?: EMPTY.life,
            colour = map["colour"] as? String ?: EMPTY.colour
        )

        val EMPTY = Fire()
    }
}