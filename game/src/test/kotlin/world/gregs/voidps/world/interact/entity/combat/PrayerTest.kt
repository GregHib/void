package world.gregs.voidps.world.interact.entity.combat

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs
import world.gregs.voidps.world.activity.combat.prayer.praying
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption

internal class PrayerTest : WorldTest() {

    @Test
    fun `Active prayers drain prayer points`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Experience.MAXIMUM_EXPERIENCE)

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 27)
        tick()
        assertTrue(player.praying("piety"))
        tickIf(limit = 1000) { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.praying("piety"))
    }

    @Test
    fun `Active curses drain prayer points`() {
        val player = createPlayer("player")
        player.experience.set(Skill.Prayer, Experience.MAXIMUM_EXPERIENCE)
        player[PrayerConfigs.PRAYERS] = "curses"

        player.interfaceOption("prayer_list", "regular_prayers", optionIndex = 0, slot = 19)
        tick()
        assertTrue(player.praying("turmoil"))
        tickIf(limit = 1000) { player.levels.get(Skill.Prayer) > 0 }

        assertEquals(0, player.levels.get(Skill.Prayer))
        assertFalse(player.praying("turmoil"))
    }

}