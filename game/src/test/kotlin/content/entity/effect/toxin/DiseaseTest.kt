package content.entity.effect.toxin

import FakeRandom
import WorldTest
import containsMessage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.setRandom
import java.util.concurrent.TimeUnit

class DiseaseTest : WorldTest() {

    @Test
    fun `Disease fades over time`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.levels.set(Skill.Attack, 3)
        player.disease(player, 3)
        assertTrue(player.containsMessage("You have been diseased"))
        assertEquals(3, player.diseaseDamage)
        assertTrue(player.timers.contains("disease"))
        assertTrue(player.diseased)
        tick(30)
        assertEquals(1, player.levels.get(Skill.Attack))
        assertEquals(2, player.diseaseDamage)
        tick(30)
        assertEquals(970, player.levels.get(Skill.Constitution))
        assertEquals(1, player.diseaseDamage)
        tick(30)
        assertEquals(0, player.diseaseDamage)
        assertFalse(player.diseased)
        assertFalse(player.timers.contains("disease"))
    }

    @Test
    fun `Anti-disease fades over time`() {
        val player = createPlayer()
        player.antiDisease(36, TimeUnit.SECONDS)
        assertEquals(-2, player.diseaseDamage)
        assertTrue(player.timers.contains("disease"))
        assertTrue(player.antiDisease)
        tick(30)
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(-1, player.diseaseDamage)
        assertTrue(player.containsMessage("Your disease resistance is about to wear off"))
        tick(30)
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(0, player.diseaseDamage)
        assertFalse(player.antiDisease)
        assertFalse(player.timers.contains("disease"))
        assertTrue(player.containsMessage("Your disease resistance has worn off"))
    }

    @Test
    fun `Can't re-disease target with lower damage`() {
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.disease(player, 10)
        assertEquals(10, player.diseaseDamage)
        assertTrue(player.timers.contains("disease"))
        tick(30)
        player.disease(player, 8)
        assertEquals(9, player.diseaseDamage)
    }

    @Test
    fun `Disease resets with higher damage`() {
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.disease(player, 10)
        assertEquals(10, player.diseaseDamage)
        assertTrue(player.timers.contains("disease"))
        tick(30)
        player.disease(player, 11)
        assertEquals(11, player.diseaseDamage)
    }

    @Test
    fun `Can't disease target with immunity`() {
        val player = createPlayer()
        player.antiDisease(1)
        player.disease(player, 10)
        assertEquals(-3, player.diseaseDamage)
        assertFalse(player.diseased)
        assertTrue(player.antiDisease)
    }
}
