package world.gregs.voidps.engine.data.definition.data

data class Catch(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 1..1
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = Catch(
            level = map["level"] as? Int ?: EMPTY.level,
            xp = map["xp"] as? Double ?: EMPTY.xp,
            chance = map["chance"] as? IntRange ?: EMPTY.chance
        )

        val EMPTY = Catch()
    }
}