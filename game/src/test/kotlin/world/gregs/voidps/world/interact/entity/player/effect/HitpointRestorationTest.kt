package world.gregs.voidps.world.interact.entity.player.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertTrue

internal class HitpointRestorationTest : WorldTest() {

    @Test
    fun `Hitpoints restore over time`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 990))
        player.levels.set(Skill.Constitution, 990)

        val drained = player.levels.drain(Skill.Constitution, 10)
        assertEquals(-10, drained)
        assertEquals(980, player.levels.get(Skill.Constitution))

        tick(11)

        assertEquals(981, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Hitpoints restore faster with rapid renewal and regen bracelet`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Level.experience(99))
        player.levels.set(Skill.Prayer, 99)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "rapid_renewal")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 990))
        player.levels.set(Skill.Constitution, 990)
        player.equipment.set(EquipSlot.Hands.index, "regen_bracelet")

        val drained = player.levels.drain(Skill.Constitution, 100)
        assertEquals(-100, drained)
        assertEquals(890, player.levels.get(Skill.Constitution))

        tick(11)

        assertEquals(902, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Hitpoints shouldn't restore if dead`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Constitution, Level.experience(Skill.Constitution, 990))
        player.levels.set(Skill.Constitution, 980)

        val drained = player.levels.drain(Skill.Constitution, 990)
        assertEquals(-980, drained)
        assertTrue(player.softTimers.contains("restore_hitpoints"))
        assertEquals(0, player.levels.get(Skill.Constitution))
        tick(11)
        assertFalse(player.softTimers.contains("restore_hitpoints"))
    }

}