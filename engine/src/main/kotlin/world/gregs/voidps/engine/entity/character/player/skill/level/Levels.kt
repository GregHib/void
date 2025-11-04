package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.Skills
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.event.EventDispatcher
import kotlin.math.max
import kotlin.math.min

class Levels(
    val levels: IntArray = defaultLevels.clone(),
) {

    interface Level {
        fun getMaxLevel(skill: Skill): Int
    }

    private lateinit var level: Level
    private lateinit var events: EventDispatcher
    private var npc: NPC? = null

    fun link(events: EventDispatcher, level: Level) {
        this.events = events
        this.level = level
        if (events is NPC) {
            npc = events
        }
    }

    /**
     * Current [skill] [level] as a percentage of [getMax] between 0-[fraction]
     */
    fun getPercent(skill: Skill, level: Int = get(skill), fraction: Double = 100.0): Double = (level / maximumLevel(skill).toDouble()) * fraction

    /**
     * Get current [skill] level
     */
    fun get(skill: Skill): Int = levels[skill.ordinal]

    /**
     * Set current [skill] [level]
     */
    fun set(skill: Skill, level: Int) {
        val previous = levels[skill.ordinal]
        levels[skill.ordinal] = level
        notify(skill, previous)
    }

    /**
     * Remove all [skill] boosts and drains
     */
    fun clear(skill: Skill) {
        set(skill, getMax(skill))
    }

    /**
     * Get max [Experience] [skill] level
     */
    fun getMax(skill: Skill): Int = level.getMaxLevel(skill)

    /**
     * Get the difference between current [skill] level and max [skill] level
     */
    fun getOffset(skill: Skill): Int = get(skill) - getMax(skill)

    /**
     * Remove all boosts and drains
     */
    fun clear() {
        for (skill in Skill.all) {
            clear(skill)
        }
    }

    /**
     * Increases [skill] by [multiplier] or [amount] until [getMax] is reached.
     */
    fun restore(skill: Skill, amount: Int = 0, multiplier: Double = 0.0): Int {
        if (getOffset(skill) >= 0) {
            return 0
        }
        val offset = multiply(getMax(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        return modify(skill, boost, get(skill), getMax(skill))
    }

    /**
     * Increases [skill] by [multiplier] or [amount] until [getMax] + [maximum] is reached.
     * [stack]'s with existing boosts or overrides if greater than.
     */
    fun boost(
        skill: Skill,
        amount: Int = 0,
        multiplier: Double = 0.0,
        stack: Boolean = false,
        maximum: Int = if (skill == Skill.Constitution) MAXIMUM_CONSTITUTION_LEVEL else MAXIMUM_BOOST_LEVEL,
    ): Int {
        val offset = multiply(minimumLevel(skill), multiplier)
        val boost = calculateAmount(amount, offset)
        val base = if (stack) get(skill) else getMax(skill)
        val maximumBoost = (base + boost).coerceAtMost(getMax(skill) + maximum)
        return modify(skill, boost, get(skill), maximumBoost)
    }

    /**
     * Decreases [skill] by [multiplier] or [amount] until reaching zero.
     * [stack] with existing drain or override if greater than.
     */
    fun drain(skill: Skill, amount: Int = 0, multiplier: Double = 0.0, stack: Boolean = true): Int {
        val offset = multiply(maximumLevel(skill), multiplier)
        val drain = calculateAmount(amount, offset)
        val minimum = if (skill == Skill.Constitution || skill == Skill.Prayer || skill == Skill.Summoning) 0 else 1
        val minimumDrain = if (stack) minimum else (getMax(skill) - drain).coerceAtLeast(minimum)
        return modify(skill, -drain, minimumDrain, get(skill))
    }

    private fun notify(skill: Skill, previous: Int) {
        val level = get(skill)
        if (events is Player) {
            Skills.changed(events as Player, skill, previous, level)
        } else if (events is NPC) {
            Skills.changed(events as NPC, skill, previous, level)
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

    private fun modify(skill: Skill, amount: Int, min: Int, max: Int): Int {
        val current = get(skill)
        val combined = current + amount
        val final = combined.coerceIn(if (min > max) max else min, if (min > max) min else max)
        set(skill, final)
        return final - current
    }

    companion object {
        private fun multiply(level: Int, multiplier: Double) = if (multiplier > 0.0) (level * multiplier).toInt() else 0

        private fun calculateAmount(amount: Int, offset: Int) = max(0, amount) + offset

        val defaultLevels = IntArray(Skill.count) {
            if (it == Skill.Constitution.ordinal) 100 else 1
        }
        private const val MAXIMUM_BOOST_LEVEL = 26
        private const val MAXIMUM_CONSTITUTION_LEVEL = MAXIMUM_BOOST_LEVEL * 10
    }
}
