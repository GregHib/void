package world.gregs.voidps.engine.entity.character

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import world.gregs.voidps.engine.entity.character.player.skill.*
import world.gregs.voidps.engine.event.Events
import kotlin.math.max
import kotlin.math.min

class Levels(
    @JsonProperty("levelOffsets")
    val offsets: MutableMap<Skill, Int> = mutableMapOf(),
) {

    interface Level {
        fun getMaxLevel(skill: Skill): Int
    }

    @JsonIgnore
    private lateinit var level: Level

    @JsonIgnore
    private lateinit var events: Events

    fun link(events: Events, level: Level) {
        this.events = events
        this.level = level
    }

    fun getPercent(skill: Skill): Double {
        return (get(skill) / getMax(skill).toDouble()) * 100.0
    }

    fun get(skill: Skill): Int {
        return getMax(skill) + getOffset(skill)
    }

    fun getMax(skill: Skill): Int {
        return level.getMaxLevel(skill)
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

    fun clear() {
        val keys = offsets.keys.toList()
        offsets.clear()
        for (skill in keys) {
            notify(skill, get(skill))
        }
    }

    fun clearOffset(skill: Skill) {
        val previous = get(skill)
        offsets.remove(skill)
        notify(skill, previous)
    }

    fun restore(skill: Skill, amount: Int = 0, multiplier: Double = 0.0): Int {
        val offset = multiply(getMax(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val minimumBoost = min(0, getOffset(skill))
        return modify(skill, boost, minimumBoost, 0)
    }

    fun boost(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = false, maximum: Int = MAXIMUM_BOOST_LEVEL): Int {
        val offset = multiply(minimumLevel(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val maximumBoost = if (stack) min(maximum, getOffset(skill) + boost) else max(getOffset(skill), boost)
        return modify(skill, boost, 0, maximumBoost)
    }

    fun drain(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = true): Int {
        val offset = multiply(maximumLevel(skill), multiplier)
        val drain = calculateAmount(amount, offset)
        val minimumDrain = if (stack) max(-getMax(skill), getOffset(skill) - drain) else min(getOffset(skill), -drain)
        return modify(skill, -drain, minimumDrain, 0)
    }

    private fun notify(skill: Skill, previous: Int) {
        val level = get(skill)
        events.emit(CurrentLevelChanged(skill, previous, level))
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

    private fun modify(skill: Skill, amount: Int, min: Int, max: Int): Int {
        val current = getOffset(skill)
        val combined = current + amount
        val final = combined.coerceIn(min, max)
        setOffset(skill, final)
        return final - current
    }

    private fun multiply(level: Int, multiplier: Double) = if (multiplier > 0.0) (level * multiplier).toInt() else 0

    private fun calculateAmount(amount: Int, offset: Int) = max(0, amount) + offset

    companion object {
        private const val MAXIMUM_BOOST_LEVEL = 24
    }
}
