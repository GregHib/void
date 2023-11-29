package world.gregs.voidps.world.interact.entity.player.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.world.interact.entity.player.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.script.WorldTest

internal class LevelRestorationTest : WorldTest() {

    @Test
    fun `Drained levels restore over time`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Mining, Level.experience(99))
        player.levels.set(Skill.Mining, 99)

        val drained = player.levels.drain(Skill.Mining, 2)
        assertEquals(-2, drained)
        assertEquals(97, player.levels.get(Skill.Mining))

        tick(101)

        assertEquals(98, player.levels.get(Skill.Mining))
    }

    @Test
    fun `Drained levels restore faster with rapid restore`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Level.experience(50))
        player.levels.set(Skill.Prayer, 50)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "rapid_restore")
        player.experience.set(Skill.Mining, Level.experience(99))
        player.levels.set(Skill.Mining, 99)

        val drained = player.levels.drain(Skill.Mining, 10)
        assertEquals(-10, drained)
        assertEquals(89, player.levels.get(Skill.Mining))

        tick(101)

        assertEquals(91, player.levels.get(Skill.Mining))
    }

    @Test
    fun `Drained prayer doesn't restore`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Level.experience(99))
        player.levels.set(Skill.Prayer, 99)

        val drained = player.levels.drain(Skill.Prayer, 1)
        assertEquals(-1, drained)
        assertEquals(98, player.levels.get(Skill.Prayer))

        tick(101)

        assertEquals(98, player.levels.get(Skill.Prayer))
    }

    @Test
    fun `Drained summoning doesn't restore`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Summoning, Level.experience(99))
        player.levels.set(Skill.Summoning, 99)

        val drained = player.levels.drain(Skill.Summoning, 1)
        assertEquals(-1, drained)
        assertEquals(98, player.levels.get(Skill.Summoning))

        tick(101)

        assertEquals(98, player.levels.get(Skill.Summoning))
    }

    @Test
    fun `Boosted levels drain over time`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Mining, Level.experience(99))
        player.levels.set(Skill.Mining, 99)

        val boosted = player.levels.boost(Skill.Mining, 1)
        assertEquals(1, boosted)
        assertEquals(100, player.levels.get(Skill.Mining))

        tick(101)

        assertEquals(99, player.levels.get(Skill.Mining))
    }

    @Test
    fun `Boosted levels drain slower with berserker`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Level.experience(99))
        player.levels.set(Skill.Prayer, 99)
        player[PrayerConfigs.PRAYERS] = "curses"
        player.addVarbit(PrayerConfigs.ACTIVE_CURSES, "berserker")
        player.experience.set(Skill.Mining, Level.experience(99))
        player.levels.set(Skill.Mining, 99)

        val boosted = player.levels.boost(Skill.Mining, 1)
        assertEquals(1, boosted)
        assertEquals(100, player.levels.get(Skill.Mining))

        tick(101)

        assertNotEquals(99, player.levels.get(Skill.Mining))

        tick(15)

        assertEquals(99, player.levels.get(Skill.Mining))
    }
}