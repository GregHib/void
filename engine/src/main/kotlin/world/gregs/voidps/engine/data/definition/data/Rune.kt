package world.gregs.voidps.engine.data.definition.data

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
    val doubleChance: Double = 0.0
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

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Rune(
            xp = map["xp"] as? Double ?: EMPTY.xp,
            pure = map["pure"] as? Boolean ?: EMPTY.pure,
            levels = (map["levels"] as? List<Int>)?.toIntArray() ?: EMPTY.levels,
            combinations = (map["combinations"] as? Map<String, List<Any>>) ?: EMPTY.combinations,
            doubleChance = (map["ourania_chance"] as? Double) ?: EMPTY.doubleChance,
        )

        val EMPTY = Rune()
    }
}