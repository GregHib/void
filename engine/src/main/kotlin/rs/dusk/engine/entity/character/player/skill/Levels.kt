package rs.dusk.engine.entity.character.player.skill

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Levels(
    private val experience: Experience,
    private val boosts: MutableMap<Skill, Int> = mutableMapOf()
) {
    private val listeners = mutableListOf<(Skill, Int, Int) -> Unit>()
    private val levelListeners = mutableListOf<(Skill, Int, Int) -> Unit>()

    fun addBoostListener(listener: (Skill, Int, Int) -> Unit) {
        listeners.add(listener)
    }

    fun addLevelUpListener(listener: (Skill, Int, Int) -> Unit) {
        levelListeners.add(listener)
    }

    init {
        experience.addListener { skill, from, to ->
            val previousLevel = getLevel(from)
            val currentLevel = getLevel(to)
            if (currentLevel > previousLevel) {
                for (listener in levelListeners) {
                    listener.invoke(skill, previousLevel, currentLevel)
                }
            }
        }
    }

    fun get(skill: Skill): Int {
        return getMax(skill) + getBoost(skill)
    }

    fun getMax(skill: Skill): Int {
        val exp = experience.get(skill)
        return getLevel(exp)
    }

    fun getBoost(skill: Skill): Int {
        return boosts[skill] ?: 0
    }

    fun setBoost(skill: Skill, level: Int) {
        val previous = get(skill)
        boosts[skill] = level
        notify(skill, previous)
    }

    fun clearBoost(skill: Skill) {
        boosts.remove(skill)
    }

    fun restore(skill: Skill, amount: Int = 0, multiplier: Double = 0.0) {
        val offset = multiply(getMax(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val minimumBoost = min(0, getBoost(skill))
        modify(skill, boost, minimumBoost, 0)
    }

    fun boost(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = false) {
        val offset = multiply(minimumLevel(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val maximumBoost = if (stack) min(MAXIMUM_BOOST_LEVEL, getBoost(skill) + boost) else max(getBoost(skill), boost)
        modify(skill, boost, 0, maximumBoost)
    }

    fun drain(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = true) {
        val offset = multiply(maximumLevel(skill), multiplier)
        val drain = calculateAmount(amount, offset)
        val current = getBoost(skill)
        val minimumDrain = if (stack) max(-getMax(skill), current - drain) else min(current, -drain)
        modify(skill, -drain, minimumDrain, 0)
    }

    private fun notify(skill: Skill, previous: Int) {
        val level = get(skill)
        for (listener in listeners) {
            listener.invoke(skill, previous, level)
        }
    }

    private fun minimumLevel(skill: Skill): Int {
        val currentLevel = get(skill)
        val maxLevel = getMax(skill)
        return min(currentLevel, maxLevel)
    }

    private fun maximumLevel(skill: Skill): Int {
        val currentLevel = get(skill)
        val maxLevel = getMax(skill)
        return max(currentLevel, maxLevel)
    }

    private fun modify(skill: Skill, amount: Int, min: Int, max: Int) {
        val current = getBoost(skill)
        val combined = current + amount
        setBoost(skill, combined.coerceIn(min, max))
    }

    private fun multiply(level: Int, multiplier: Double) = if (multiplier > 0.0) (level * multiplier).toInt() else 0

    private fun calculateAmount(amount: Int, offset: Int) = max(0, amount) + offset

    companion object {
        private fun getLevel(experience: Double): Int {
            var points = 0
            var output: Int
            for (level in 1..99) {
                points += floor(level + 300.0 * 2.0.pow(level / 7.0)).toInt()
                output = points / 4
                if (output - 1 >= experience) {
                    return level
                }
            }
            return 99
        }

        fun createLevels(experience: Experience): IntArray {
            return Skill.all.map { skill -> getLevel(experience.get(skill)) }.toIntArray()
        }

        private const val MAXIMUM_BOOST_LEVEL = 24
    }
}
