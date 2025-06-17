package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

/**
 * @param xp experience from successfully crafting a rune
 * @param pure whether pure essence is required
 * @param levels level required for each amount of runes created
 * @param doubleChance of getting double at ourania altar with medium ardougne diary
 */
data class Rune(
    val xp: Double = 0.0,
    val pure: Boolean = false,
    val levels: IntArray = intArrayOf(),
    val combinations: Map<String, List<Any>> = emptyMap(),
    val doubleChance: Double = 0.0,
) {
    fun multiplier(player: Player): Int {
        var multiplier = 1
        for (index in levels.indices.reversed()) {
            if (player.levels.get(Skill.Runecrafting) >= levels[index]) {
                multiplier = index + 1
                break
            }
        }
        return multiplier
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Rune

        if (xp != other.xp) return false
        if (pure != other.pure) return false
        return levels.contentEquals(other.levels)
    }

    override fun hashCode(): Int {
        var result = xp.hashCode()
        result = 31 * result + pure.hashCode()
        result = 31 * result + levels.contentHashCode()
        return result
    }

    companion object {
        operator fun invoke(reader: ConfigReader): Rune {
            var xp = 0.0
            var pure = false
            val levels = IntArrayList()
            val combinations = Object2ObjectOpenHashMap<String, List<Any>>(3, Hash.VERY_FAST_LOAD_FACTOR)
            var doubleChance = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "xp" -> xp = reader.double()
                    "pure" -> pure = reader.boolean()
                    "levels" -> while (reader.nextElement()) {
                        levels.add(reader.int())
                    }
                    "combinations" -> while (reader.nextEntry()) {
                        combinations[reader.key()] = reader.list()
                    }
                    "ourania_chance" -> doubleChance = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Rune(xp = xp, pure = pure, levels = levels.toIntArray(), combinations = combinations, doubleChance = doubleChance)
        }

        val EMPTY = Rune()
    }
}
