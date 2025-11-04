package world.gregs.voidps.engine.entity.character.player.skill

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp

internal class ExperienceTableTest {

    private lateinit var experience: Experience
    private lateinit var events: Player

    @BeforeEach
    fun setup() {
        events = mockk(relaxed = true)
        experience = Experience(maximum = 200.0)
        experience.events = events
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
    fun `Add experience with 10x rate`() {
        Settings.load(mapOf("world.experienceRate" to "10.0"))
        experience = Experience(maximum = 500.0)
        experience.events = events
        experience.add(Skill.Attack, 10.0)
        experience.add(Skill.Attack, 10.0)
        assertEquals(200.0, experience.get(Skill.Attack))
        Settings.clear()
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
        mockkObject(Skills)
        experience.set(Skill.Defence, 100.0)
        experience.addBlock(Skill.Defence)
        experience.add(Skill.Defence, 100.0)
        assertEquals(100.0, experience.get(Skill.Defence))
        verify {
            Skills.exp(events, Skill.Defence, 0.0, 100.0)
        }
        unmockkObject(Skills)
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
        mockkObject(Skills)
        experience.set(Skill.Attack, 100.0)
        experience.add(Skill.Attack, 10.0)
        verify { Skills.exp(events, Skill.Attack, 100.0, 110.0) }
        unmockkObject(Skills)
    }

    @Test
    fun `Listen for blocked exp`() {
        mockkObject(Skills)
        experience.set(Skill.Attack, 100.0)
        experience.addBlock(Skill.Attack)
        experience.add(Skill.Attack, 10.0)
        verify { Skills.blocked(events, Skill.Attack, 10.0) }
        unmockkObject(Skills)
    }

    @Test
    fun `Check if blocked`() {
        experience.addBlock(Skill.Attack)
        assertTrue(experience.blocked(Skill.Attack))
        experience.removeBlock(Skill.Attack)
        assertFalse(experience.blocked(Skill.Attack))
    }

    @Test
    fun `Add experience extension`() {
        mockkStatic("world.gregs.voidps.engine.entity.character.player.skill.exp.ExperienceKt")
        val player: Player = mockk(relaxed = true)
        every { player.experience } returns experience
        player.exp(Skill.Attack, 10.0)
        player.exp(Skill.Attack, 10.0)
        assertEquals(20.0, experience.get(Skill.Attack))
    }
}
