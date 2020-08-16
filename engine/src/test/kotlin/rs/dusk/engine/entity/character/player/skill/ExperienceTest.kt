package rs.dusk.engine.entity.character.player.skill

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.character.player.Player

internal class ExperienceTest {

    lateinit var experience: Experience

    @BeforeEach
    fun setup() {
        experience = Experience(maximum = 200.0)
    }

    @Test
    fun `Get experience`() {
        assertEquals(0.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Set experience`() {
        experience.set(Skill.Attack, 100.0)
        experience.set(Skill.Strength, 123.0)
        assertEquals(100.0, experience.get(Skill.Attack))
        assertEquals(123.0, experience.get(Skill.Strength))
    }

    @Test
    fun `Add experience`() {
        experience.add(Skill.Attack, 10.0)
        experience.add(Skill.Attack, 10.0)
        assertEquals(20.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Can't set negative experience`() {
        experience.set(Skill.Attack, -10.0)
        assertEquals(0.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Can't add negative experience`() {
        experience.add(Skill.Attack, 10.0)
        experience.add(Skill.Attack, -10.0)
        assertEquals(10.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Experience can't exceed maximum`() {
        experience.set(Skill.Attack, 10.0)
        experience.set(Skill.Attack, 210.0)
        experience.add(Skill.Attack, 200.0)
        assertEquals(10.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Experience can equal maximum`() {
        experience.add(Skill.Attack, 200.0)
        assertEquals(200.0, experience.get(Skill.Attack))
    }

    @Test
    fun `Experience for blocked skills aren't changed`() {
        experience.set(Skill.Defence, 100.0)
        var called = false
        experience.addListener { _, _, _ ->
            called = true
        }
        experience.addBlock(Skill.Defence)
        experience.add(Skill.Defence, 100.0)
        assertEquals(100.0, experience.get(Skill.Defence))
        assertFalse(called)
    }

    @Test
    fun `Removed blocks give experience`() {
        experience.addBlock(Skill.Defence)
        experience.removeBlock(Skill.Defence)
        experience.add(Skill.Defence, 100.0)
        assertEquals(100.0, experience.get(Skill.Defence))
    }

    @Test
    fun `Notified of change`() {
        var called = false
        experience.set(Skill.Attack, 100.0)
        experience.addListener { skill, from, to ->
            assertEquals(Skill.Attack, skill)
            assertEquals(100.0, from)
            assertEquals(110.0, to)
            called = true
        }
        experience.add(Skill.Attack, 10.0)
        assertTrue(called)
    }

    @Test
    fun `Removed listener isn't notified`() {
        var called = false
        val listener: (Skill, Double, Double) -> Unit = { _, _, _ ->
            called = true
        }
        experience.addListener(listener)
        experience.removeListener(listener)
        experience.add(Skill.Attack, 10.0)
        assertFalse(called)
    }

    @Test
    fun `Listen for blocked exp`() {
        var called = false
        experience.set(Skill.Attack, 100.0)
        experience.addBlockedListener { skill, amount ->
            assertEquals(Skill.Attack, skill)
            assertEquals(10.0, amount)
            called = true
        }
        experience.addBlock(Skill.Attack)
        experience.add(Skill.Attack, 10.0)
        assertTrue(called)
    }

    @Test
    fun `Blocked listener ignores unblocked exp`() {
        var called = false
        experience.addBlockedListener { _, _ ->
            called = true
        }
        experience.add(Skill.Attack, 10.0)
        assertFalse(called)
    }

    @Test
    fun `Check if blocked`() {
        experience.addBlock(Skill.Attack)
        assertTrue(experience.blocked(Skill.Attack))
        experience.removeBlock(Skill.Attack)
        assertFalse(experience.blocked(Skill.Attack))
    }

    @Test
    fun `Removed listener for blocked exp`() {
        var called = false
        val listener: (Skill, Double) -> Unit = { _, _ ->
            called = true
        }
        experience.addBlockedListener(listener)
        experience.removeBlockedListener(listener)
        experience.add(Skill.Attack, 10.0)
        assertFalse(called)
    }

    @Test
    fun `Add experience extension`() {
        mockkStatic("rs.dusk.engine.entity.character.player.skill.ExperienceKt")
        val player: Player = mockk(relaxed = true)
        every { player.experience } returns experience
        player.addExp(Skill.Attack, 10.0)
        player.addExp(Skill.Attack, 10.0)
        assertEquals(20.0, experience.get(Skill.Attack))
    }

}