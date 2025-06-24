package world.gregs.voidps.engine.entity.character.player.skill

import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.MaxLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.event.EventDispatcher

internal class LevelsTest {

    private lateinit var exp: Experience
    private lateinit var levels: Levels
    private lateinit var events: EventDispatcher

    @BeforeEach
    fun setup() {
        exp = Experience(maximum = 10000.0)
        events = mockk(relaxed = true)
        levels = Levels()
        exp.events = events
        levels.link(events, PlayerLevels(exp))
    }

    @Test
    fun `Get skill level`() {
        assertEquals(1, levels.get(Skill.Attack))
        assertEquals(100, levels.get(Skill.Constitution))
        assertEquals(0, levels.getOffset(Skill.Attack))
    }

    @Test
    fun `Set level`() {
        levels.set(Skill.Attack, 4)
        assertEquals(4, levels.get(Skill.Attack))
    }

    @Test
    fun `Get maximum level`() {
        levels.set(Skill.Attack, 10)
        assertEquals(1, levels.getMax(Skill.Attack))
    }

    @Test
    fun `Get boosted offset`() {
        levels.set(Skill.Attack, 10)
        assertEquals(9, levels.getOffset(Skill.Attack))
    }

    @Test
    fun `Get drained offset`() {
        exp.set(Skill.Attack, 1154.0)
        assertEquals(-9, levels.getOffset(Skill.Attack))
    }

    @Test
    fun `Clear level boost`() {
        levels.set(Skill.Attack, 10)
        levels.clear(Skill.Attack)
        assertEquals(1, levels.get(Skill.Attack))
    }

    @Test
    fun `Boost by fixed level`() {
        assertEquals(4, levels.boost(Skill.Attack, amount = 4))
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Boost by level multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        assertEquals(5, levels.boost(Skill.Attack, multiplier = 0.5))
        assertEquals(15, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't boost by negative amount or multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        assertEquals(0, levels.boost(Skill.Attack, -10, -0.2))
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting does not stack`() {
        assertEquals(4, levels.boost(Skill.Attack, 4))
        assertEquals(0, levels.boost(Skill.Attack, 2))
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting does stack with drains`() {
        levels.set(Skill.Attack, 0)
        assertEquals(2, levels.boost(Skill.Attack, 2))
        assertEquals(2, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't drain lower than zero`() {
        levels.set(Skill.Attack, 2)
        assertEquals(-1, levels.drain(Skill.Attack, 2))
        assertEquals(1, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting with stack`() {
        assertEquals(4, levels.boost(Skill.Attack, 4, stack = true))
        assertEquals(2, levels.boost(Skill.Attack, 2, stack = true))
        assertEquals(7, levels.get(Skill.Attack))
    }

    @Test
    fun `Boosting with stack has arbitrary limit`() {
        exp = Experience()
        exp.events = events
        levels.link(events, PlayerLevels(exp))

        exp.set(Skill.Strength, 14000000.0)
        levels.set(Skill.Strength, 99)

        val amount = levels.boost(Skill.Strength, 100, stack = true)
        assertEquals(26, amount)
        assertEquals(125, levels.get(Skill.Strength))
    }

    @Test
    fun `Boost constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.set(Skill.Constitution, 100)
        val amount = levels.boost(Skill.Constitution, amount = 40)
        assertEquals(40, amount)
        assertEquals(140, levels.get(Skill.Constitution))
    }

    @Test
    fun `Boost constitution up to a fixed maximum`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.set(Skill.Constitution, 90)
        val amount = levels.boost(Skill.Constitution, amount = 500, stack = true, maximum = 100)
        assertEquals(110, amount)
        assertEquals(200, levels.get(Skill.Constitution))
    }

    @Test
    fun `Drain by fixed level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        val amount = levels.drain(Skill.Attack, amount = 4)
        assertEquals(-4, amount)
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Drain by multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        val amount = levels.drain(Skill.Attack, multiplier = 0.5)
        assertEquals(-5, amount)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't drain by negative amount and multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        val amount = levels.drain(Skill.Attack, -10, -0.2)
        assertEquals(0, amount)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining stacks`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        assertEquals(-4, levels.drain(Skill.Attack, 4))
        assertEquals(-2, levels.drain(Skill.Attack, 2))
        assertEquals(4, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining stacks has minimum`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        val amount = levels.drain(Skill.Attack, 11)
        assertEquals(-9, amount)
        assertEquals(1, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining without stacking`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        assertEquals(-4, levels.drain(Skill.Attack, 4, stack = false))
        assertEquals(0, levels.drain(Skill.Attack, 2, stack = false))
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Draining without stack still stacks with boosts`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 11)
        val amount = levels.drain(Skill.Attack, 2, stack = false)
        assertEquals(-2, amount)
        assertEquals(9, levels.get(Skill.Attack))
    }

    @Test
    fun `Drain constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.set(Skill.Constitution, 100)
        val amount = levels.drain(Skill.Constitution, amount = 40)
        assertEquals(-40, amount)
        assertEquals(60, levels.get(Skill.Constitution))
    }

    @Test
    fun `Drain constitution by less than boosted amount`() {
        exp.set(Skill.Constitution, 14000000.0)
        levels.set(Skill.Constitution, 1050)
        val amount = levels.drain(Skill.Constitution, amount = 40)
        assertEquals(-40, amount)
        assertEquals(1010, levels.get(Skill.Constitution))
    }

    @Test
    fun `Restore by fixed level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 1)
        val amount = levels.restore(Skill.Attack, amount = 4)
        assertEquals(4, amount)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore doesn't change boosted levels`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 15)
        val amount = levels.restore(Skill.Attack, amount = 4)
        assertEquals(0, amount)
        assertEquals(15, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore by multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 1)
        val amount = levels.restore(Skill.Attack, multiplier = 0.5)
        assertEquals(5, amount)
        assertEquals(6, levels.get(Skill.Attack))
    }

    @Test
    fun `Can't restore by negative amount or multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 10)
        val amount = levels.restore(Skill.Attack, -10, -0.2)
        assertEquals(0, amount)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore can't exceed max level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.set(Skill.Attack, 8)
        val amount = levels.restore(Skill.Attack, amount = 5)
        assertEquals(2, amount)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.set(Skill.Constitution, 10)
        val amount = levels.restore(Skill.Constitution, amount = 40)
        assertEquals(40, amount)
        assertEquals(50, levels.get(Skill.Constitution))
    }

    @Test
    fun `Listen to boost change`() {
        exp.set(Skill.Magic, 1154.0)
        levels.set(Skill.Magic, 9)
        levels.set(Skill.Magic, 12)
        verify {
            events.emit(CurrentLevelChanged(Skill.Magic, 9, 12))
        }
    }

    @Test
    fun `Listen to level up`() {
        exp.set(Skill.Magic, 1154.0)
        verifyOrder {
            events.emit(any<MaxLevelChanged>()) // (Skill.Magic, 1, 10))
        }
    }
}
