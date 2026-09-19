package content.entity.player.stat

import WorldTest
import containsMessage
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
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
    fun `Reloading the settings starts and ends the event for players already online`() {
        enable(false)
        val player = createPlayer()
        try {
            enable(true)
            SettingsReload.now()

            assertTrue(player["bonus_xp_enabled", false])
            assertEquals(2.7, player.experience.multiplier)

            enable(false)
            SettingsReload.now()

            assertFalse(player["bonus_xp_enabled", false])
            assertEquals(1.0, player.experience.multiplier)
        } finally {
            enable(false)
        }
    }

    @Test
    fun `Progress from before the event doesn't count towards it`() {
        enable(false)
        val player = createPlayer()
        player["bonus_xp_time"] = 600
        player["bonus_xp_counter"] = 5000
        try {
            enable(true)
            SettingsReload.now()

            assertEquals(0, player["bonus_xp_time", 0])
            assertEquals(0, player["bonus_xp_counter", 0])
            assertEquals(2.7, player.experience.multiplier)
        } finally {
            enable(false)
        }
    }

    @Test
    fun `Logging in with progress from a previous event starts fresh`() {
        enable(true)
        try {
            // Saved by an event which finished some time ago
            val player = createPlayer(name = "returning") { saved ->
                saved["bonus_xp_time"] = 600
                saved["bonus_xp_counter"] = 5000
                saved["bonus_xp_event"] = 1
            }

            assertEquals(0, player["bonus_xp_time", 0])
            assertEquals(0, player["bonus_xp_counter", 0])
            assertEquals(2.7, player.experience.multiplier)
        } finally {
            enable(false)
        }
    }

    @Test
    fun `Logging back in during the same event keeps its progress`() {
        enable(true)
        try {
            // Whichever player starts the event stamps it
            createPlayer(name = "first")
            val player = createPlayer(name = "relogged") { saved ->
                saved["bonus_xp_time"] = 31
                saved["bonus_xp_counter"] = 5000
                saved["bonus_xp_event"] = BonusExperience.event
            }

            assertEquals(31, player["bonus_xp_time", 0])
            assertEquals(5000, player["bonus_xp_counter", 0])
            assertEquals(2.55, player.experience.multiplier)
        } finally {
            enable(false)
        }
    }

    @Test
    fun `The event a player took part in is kept in their save`() {
        enable(true)
        try {
            val player = createPlayer(name = "stamped")

            // Persisted variables live in data, session ones in temp
            assertTrue((player.variables as PlayerVariables).data.containsKey("bonus_xp_event"))
        } finally {
            enable(false)
        }
    }

    @Test
    fun `Ending the event leaves nothing behind to carry into the next one`() {
        enable(true)
        val player = createPlayer(name = "ended")
        player["bonus_xp_time"] = 30
        player["bonus_xp_counter"] = 5000

        enable(false)
        SettingsReload.now()

        assertEquals(0, player["bonus_xp_event", 0])
        assertEquals(0, player["bonus_xp_time", 0])
        assertEquals(0, player["bonus_xp_counter", 0])
        assertEquals(0, BonusExperience.event)
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
