package rs.dusk.engine.entity.character.player.skill

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LevelsTest {

    lateinit var exp: Experience
    lateinit var levels: Levels

    @BeforeEach
    fun setup() {
        exp = Experience(maximum = 10000.0)
        levels = Levels(exp)
    }

    @Test
    fun `Get skill level`() {
        assertEquals(1, levels.get(Skill.Attack))
        assertEquals(10, levels.get(Skill.Constitution))
    }

    @Test
    fun `Set level boost`() {
        levels.setBoost(Skill.Attack, 3)
        assertEquals(3, levels.getBoost(Skill.Attack))
        assertEquals(4, levels.get(Skill.Attack))
    }

    @Test
    fun `Get maximum level`() {
        levels.setBoost(Skill.Attack, 10)
        assertEquals(1, levels.getMax(Skill.Attack))
    }

    @Test
    fun `Clear level boost`() {
        levels.setBoost(Skill.Attack, 10)
        levels.clearBoost(Skill.Attack)
        assertEquals(1, levels.get(Skill.Attack))
    }

    @Test
    fun `Boost by fixed level`() {
        levels.boost(Skill.Attack, amount = 4)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Boost by level multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.boost(Skill.Attack, multiplier = 0.5)
        assertEquals(15, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't boost by negative amount or multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.boost(Skill.Attack, -10, -0.2)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting does not stack`() {
        levels.boost(Skill.Attack, 4)
        levels.boost(Skill.Attack, 2)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting does stack with drains`() {
        levels.setBoost(Skill.Attack, -1)
        levels.boost(Skill.Attack, 2)
        assertEquals(2, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting with stack`() {
        levels.boost(Skill.Attack, 4, stack = true)
        levels.boost(Skill.Attack, 2, stack = true)
        assertEquals(7, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting with stack has arbitrary limit`() {
        exp.set(Skill.Strength, 14000000.0)
        levels.boost(Skill.Strength, 100, stack = true)
        assertEquals(25, levels.get(Skill.Strength))
    }

    @Test
    fun `Drain by fixed level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.drain(Skill.Attack, amount = 4)
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Drain by multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.drain(Skill.Attack, multiplier = 0.5)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't drain by negative amount or multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.boost(Skill.Attack, -10, -0.2)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining stacks`() {
        exp.set(Skill.Attack, 1154.0)
        levels.drain(Skill.Attack, 4)
        levels.drain(Skill.Attack, 2)
        assertEquals(4, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining stacks has minimum`() {
        exp.set(Skill.Attack, 1154.0)
        levels.drain(Skill.Attack, 11)
        assertEquals(0, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining without stack`() {
        exp.set(Skill.Attack, 1154.0)
        levels.drain(Skill.Attack, 4, stack = false)
        levels.drain(Skill.Attack, 2, stack = false)
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining without stack still stacks with boosts`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setBoost(Skill.Attack, 1)
        levels.drain(Skill.Attack, 2, stack = false)
        assertEquals(9, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore by fixed level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setBoost(Skill.Attack, -9)
        levels.restore(Skill.Attack, amount = 4)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore by multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setBoost(Skill.Attack, -9)
        levels.restore(Skill.Attack, multiplier = 0.5)
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't restore by negative amount or multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.restore(Skill.Attack, -10, -0.2)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore can't exceed max level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setBoost(Skill.Attack, -2)
        levels.restore(Skill.Attack, amount = 5)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Listen to boost change`() {
        exp.set(Skill.Magic, 1154.0)
        levels.setBoost(Skill.Magic, -1)
        var called = false
        levels.addBoostListener { skill, from, to ->
            assertEquals(Skill.Magic, skill)
            assertEquals(9, from)
            assertEquals(12, to)
            called = true
        }
        levels.setBoost(Skill.Magic, 2)
        assertTrue(called)
    }

    @Test
    fun `Listen to level up`() {
        var called = false
        levels.addLevelUpListener { skill, from, to ->
            assertEquals(Skill.Magic, skill)
            assertEquals(1, from)
            assertEquals(10, to)
            called = true
        }
        exp.set(Skill.Magic, 1154.0)
        assertTrue(called)
    }

}