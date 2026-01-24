package content.entity.effect.toxin

import WorldTest
import containsMessage
import messages
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import java.util.concurrent.TimeUnit

class PoisonTest : WorldTest() {

    @Test
    fun `Poison fades over time`() {
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.poison(player, 14)
        assertTrue(player.containsMessage("You have been poisoned"))
        assertEquals(14, player.poisonDamage)
        assertTrue(player.timers.contains("poison"))
        assertTrue(player.poisoned)
        tick(30)
        assertEquals(976, player.levels.get(Skill.Constitution))
        assertEquals(12, player.poisonDamage)
        tick(30)
        assertEquals(976, player.levels.get(Skill.Constitution))
        assertEquals(0, player.poisonDamage)
        assertFalse(player.poisoned)
        assertFalse(player.timers.contains("poison"))
    }

    @Test
    fun `Anti-poison fades over time`() {
        val player = createPlayer()
        player.antiPoison(1600, TimeUnit.MILLISECONDS)
        assertEquals(-4, player.poisonDamage)
        assertTrue(player.timers.contains("poison"))
        assertTrue(player.antiPoison)
        tick(30)
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(-2, player.poisonDamage)
        assertTrue(player.containsMessage("Your poison resistance is about to wear off"))
        tick(30)
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(0, player.poisonDamage)
        assertFalse(player.antiPoison)
        assertFalse(player.timers.contains("poison"))
        assertTrue(player.containsMessage("Your poison resistance has worn off"))
    }

    @Test
    fun `Can't re-poison target with lower damage`() {
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.poison(player, 100)
        assertEquals(100, player.poisonDamage)
        assertTrue(player.timers.contains("poison"))
        tick(30)
        player.poison(player, 90)
        assertEquals(890, player.levels.get(Skill.Constitution))
        assertEquals(98, player.poisonDamage)
    }

    @Test
    fun `Poison resets with higher damage`() {
        val player = createPlayer()
        player.levels.set(Skill.Constitution, 990)
        player.poison(player, 100)
        assertEquals(100, player.poisonDamage)
        assertTrue(player.timers.contains("poison"))
        tick(30)
        player.poison(player, 110)
        assertEquals(890, player.levels.get(Skill.Constitution))
        assertEquals(110, player.poisonDamage)
    }

    @Test
    fun `Can't poison target with immunity`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Shield.index, "anti_poison_totem")

        player.poison(player, 100)
        assertEquals(0, player.poisonDamage)
        assertFalse(player.poisoned)
        assertFalse(player.antiPoison)
    }

    @Test
    fun `Can't poison target with anti-poison`() {
        val player = createPlayer()
        player.antiPoison(1600, TimeUnit.MILLISECONDS)
        assertTrue(player.antiPoison)

        player.poison(player, 100)
        assertEquals(-4, player.poisonDamage)
        assertTrue(player.antiPoison)
    }
}