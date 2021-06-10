package world.gregs.voidps.engine.entity.character.player.skill

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Events
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Levels(
    @JsonProperty("levelOffsets")
    val offsets: MutableMap<Skill, Int> = mutableMapOf(),
) {
    @JsonIgnore
    lateinit var experience: Experience

    @JsonIgnore
    private lateinit var events: Events

    fun link(experience: Experience, events: Events) {
        this.experience = experience
        this.events = events
        events.on<Player, GrantExp> {
            val previousLevel = getLevel(from)
            val currentLevel = getLevel(to)
            if (currentLevel > previousLevel) {
                events.emit(Leveled(skill, previousLevel, currentLevel))
            }
        }
    }

    fun get(skill: Skill): Int {
        return getMax(skill) + getOffset(skill)
    }

    fun getMax(skill: Skill): Int {
        val exp = experience.get(skill)
        return getLevel(exp)
    }

    fun getOffset(skill: Skill): Int {
        return offsets[skill] ?: 0
    }

    fun setOffset(skill: Skill, offset: Int) {
        if (offset == 0) {
            clearOffset(skill)
        } else {
            val previous = get(skill)
            offsets[skill] = offset
            notify(skill, previous)
        }
    }

    fun clearOffset(skill: Skill) {
        val previous = get(skill)
        offsets.remove(skill)
        notify(skill, previous)
    }

    fun restore(skill: Skill, amount: Int = 0, multiplier: Double = 0.0) {
        val offset = multiply(getMax(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val minimumBoost = min(0, getOffset(skill))
        modify(skill, boost, minimumBoost, 0)
    }

    fun boost(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = false) {
        val offset = multiply(minimumLevel(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val maximumBoost = if (stack) min(MAXIMUM_BOOST_LEVEL, getOffset(skill) + boost) else max(getOffset(skill), boost)
        modify(skill, boost, 0, maximumBoost)
    }

    fun drain(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = true) {
        val offset = multiply(maximumLevel(skill), multiplier)
        val drain = calculateAmount(amount, offset)
        val current = getOffset(skill)
        val minimumDrain = if (stack) max(-getMax(skill), current - drain) else min(current, -drain)
        modify(skill, -drain, minimumDrain, 0)
    }

    private fun notify(skill: Skill, previous: Int) {
        val level = get(skill)
        events.emit(LevelChanged(skill, previous, level))
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
        val current = getOffset(skill)
        val combined = current + amount
        setOffset(skill, combined.coerceIn(min, max))
    }

    private fun multiply(level: Int, multiplier: Double) = if (multiplier > 0.0) (level * multiplier).toInt() else 0

    private fun calculateAmount(amount: Int, offset: Int) = max(0, amount) + offset

    companion object {
        private fun getLevel(experience: Double): Int {
            var total = 0
            return (1..99).firstOrNull { level ->
                total += experience(level)
                total / 4 - 1 >= experience
            } ?: 99
        }

        fun getExperience(level: Int): Int = (1 until level)
            .sumBy(::experience) / 4

        private fun experience(level: Int) = (level + 300.0 * 2.0.pow(level / 7.0)).toInt()

        fun createLevels(experience: Experience): IntArray {
            return Skill.all.map { skill -> getLevel(experience.get(skill)) }.toIntArray()
        }

        private const val MAXIMUM_BOOST_LEVEL = 24
    }
}
