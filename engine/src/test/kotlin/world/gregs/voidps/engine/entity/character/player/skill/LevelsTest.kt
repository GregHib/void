package world.gregs.voidps.engine.entity.character.player.skill

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerLevels
import world.gregs.voidps.engine.event.Events

internal class LevelsTest {

    lateinit var exp: Experience
    lateinit var levels: Levels
    lateinit var events: Events

    @BeforeEach
    fun setup() {
        exp = Experience(maximum = 10000.0)
        events = spyk(Events(mockk<Player>(relaxed = true)))
        levels = Levels()
        exp.events = events
        levels.link(events, PlayerLevels(exp))
    }

    @Test
    fun `Get skill level`() {
        assertEquals(1, levels.get(Skill.Attack))
        assertEquals(100, levels.get(Skill.Constitution))
    }

    @Test
    fun `Set level boost`() {
        levels.setOffset(Skill.Attack, 3)
        assertEquals(3, levels.getOffset(Skill.Attack))
        assertEquals(4, levels.get(Skill.Attack))
    }

    @Test
    fun `Get maximum level`() {
        levels.setOffset(Skill.Attack, 10)
        assertEquals(1, levels.getMax(Skill.Attack))
    }

    @Test
    fun `Clear level boost`() {
        levels.setOffset(Skill.Attack, 10)
        levels.clearOffset(Skill.Attack)
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
        levels.setOffset(Skill.Attack, -1)
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
    fun `Boost constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.boost(Skill.Constitution, amount = 40)
        assertEquals(140, levels.get(Skill.Constitution))
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
        levels.setOffset(Skill.Attack, 1)
        levels.drain(Skill.Attack, 2, stack = false)
        assertEquals(9, levels.get(Skill.Attack))
    }

    @Test
    fun `Drain constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.drain(Skill.Constitution, amount = 40)
        assertEquals(60, levels.get(Skill.Constitution))
    }

    @Test
    fun `Restore by fixed level`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setOffset(Skill.Attack, -9)
        levels.restore(Skill.Attack, amount = 4)
        assertEquals(5, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore by multiplier`() {
        exp.set(Skill.Attack, 1154.0)
        levels.setOffset(Skill.Attack, -9)
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
        levels.setOffset(Skill.Attack, -2)
        levels.restore(Skill.Attack, amount = 5)
        assertEquals(10, levels.get(Skill.Attack))
    }

    @Test
    fun `Restore constitution level`() {
        exp.set(Skill.Constitution, 1154.0)
        levels.setOffset(Skill.Constitution, -90)
        levels.restore(Skill.Constitution, amount = 40)
        assertEquals(50, levels.get(Skill.Constitution))
    }

    @Test
    fun `Listen to boost change`() {
        exp.set(Skill.Magic, 1154.0)
        levels.setOffset(Skill.Magic, -1)
        levels.setOffset(Skill.Magic, 2)
        verify {
            events.emit(LevelChanged(Skill.Magic, 9, 12))
        }
    }

    @Test
    fun `Listen to level up`() {
        exp.set(Skill.Magic, 1154.0)
        verify {
            events.emit(any<Leveled>())//(Skill.Magic, 1, 10))
        }
    }

}