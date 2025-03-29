package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

data class Catch(
    val level: Int = 1,
    val xp: Double = 0.0,
    val chance: IntRange = 1..1
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Catch {
            var level = 0
            var xp = 0.0
            var chanceMin = 1
            var chanceMax = 1
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "chance_min" -> chanceMin = reader.int()
                    "chance_max" -> chanceMax = reader.int()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Catch(level = level, xp = xp, chance = chanceMin until chanceMax)
        }

        val EMPTY = Catch()
    }
}