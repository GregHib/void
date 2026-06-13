package content.entity.player.stat

import WorldTest
import containsMessage
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import java.util.Properties
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BonusExperienceWorldTest : WorldTest() {

    private fun enable(enabled: Boolean = true) {
        val properties = Properties()
        properties.putAll(settings)
        properties["events.bonusExperience.enabled"] = enabled.toString()
        Settings.load(properties)
    }

    @Test
    fun `Bonus experience is granted and tracked while active`() {
        enable()
        val player = createPlayer()

        assertTrue(player["bonus_xp_enabled", false])
        assertEquals(2.7, player.experience.multiplier)
        assertTrue(player.containsMessage("Bonus XP Weekend"))

        player.exp(Skill.Attack, 10.0)
        assertEquals(270, player.experience.direct(Skill.Attack))
        assertEquals(170, player["bonus_xp_counter", 0])
    }

    @Test
    fun `Multiplier decreases with time spent online`() {
        enable()
        val player = createPlayer()
        player["bonus_xp_time"] = 30

        tick(101)

        assertEquals(31, player["bonus_xp_time", 0])
        assertEquals(2.55, player.experience.multiplier)
    }

    @Test
    fun `Bonus experience progress resets when inactive`() {
        enable(false)
        val player = createPlayer()
        player["bonus_xp_time"] = 60
        player["bonus_xp_counter"] = 100

        scripts.filterIsInstance<BonusExperience>().first().reset(player)

        assertFalse(player["bonus_xp_enabled", false])
        assertEquals(1.0, player.experience.multiplier)
        assertEquals(0, player["bonus_xp_time", 0])
        assertEquals(0, player["bonus_xp_counter", 0])

        player.exp(Skill.Attack, 10.0)
        assertEquals(100, player.experience.direct(Skill.Attack))
        assertEquals(0, player["bonus_xp_counter", 0])
    }
}
