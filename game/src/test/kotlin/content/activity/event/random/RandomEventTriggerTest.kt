package content.activity.event.random

import WorldTest
import content.bot.Bot
import content.quest.instance
import io.mockk.mockk
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RandomEventTriggerTest : WorldTest() {

    @Test
    fun `Starts a random event once cooldown expires`() {
        val player = createPlayer(Tile(3221, 3218), "re_trigger")
        player["random_event_cooldown"] = 1

        RandomEvents.roll(player)
        tick(10)

        assertEquals("maze", player["random_event"])
        assertNotNull(player.instance())
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `First event arms the cooldown without an event`() {
        val player = createPlayer(Tile(3221, 3218), "re_cooldown")

        RandomEvents.roll(player)
        tick(10)

        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Bots don't receive random events`() {
        val player = createPlayer(Tile(3221, 3218), "re_bot") { it["bot"] = mockk<Bot>(relaxed = true) }
        player["random_event_cooldown"] = 1

        RandomEvents.roll(player)
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `No random event during another random event`() {
        val player = createPlayer(Tile(3221, 3218), "re_active")
        player["random_event"] = "maze"
        player["random_event_cooldown"] = 1

        RandomEvents.roll(player)
        tick()

        assertFalse(player.queue.contains("random_event_start"))
    }

    @Test
    fun `Players who opt out don't receive random events`() {
        Settings.load(mapOf("events.randomEvents.optOut" to "true"))
        val player = createPlayer(Tile(3221, 3218), "re_opt_out")
        player["random_event_cooldown"] = 1
        player["random_events_disabled"] = true

        RandomEvents.roll(player)
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `Opting out is ignored on worlds without the opt out setting`() {
        Settings.load(mapOf("events.randomEvents.optOut" to "false"))
        val player = createPlayer(Tile(3221, 3218), "re_opt_out_off")
        player["random_event_cooldown"] = 1
        player["random_events_disabled"] = true

        RandomEvents.roll(player)
        tick(10)

        assertEquals("maze", player["random_event"])
    }

    @Test
    fun `No random event in areas tagged no_random_events`() {
        val player = createPlayer(Tile(3098, 3107), "re_tutorial")
        player["random_event_cooldown"] = 1

        RandomEvents.roll(player)
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `Experience drop starts a random event by default`() {
        Settings.load(mapOf("events.randomEvents.chance" to "1"))
        val player = createPlayer(Tile(3221, 3218), "re_experience")
        player["random_event_cooldown"] = 1

        player.exp(Skill.Attack, 10.0)
        tick(10)

        assertEquals("maze", player["random_event"])
    }

    @Test
    fun `Experience drop doesn't start a random event with timer trigger`() {
        Settings.load(mapOf("events.randomEvents.trigger" to "timer", "events.randomEvents.chance" to "1"))
        val player = createPlayer(Tile(3221, 3218), "re_experience_timer")
        player["random_event_cooldown"] = 1

        player.exp(Skill.Attack, 10.0)
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `Timer only runs with timer trigger`() {
        Settings.load(mapOf("events.randomEvents.trigger" to "timer"))
        val timed = createPlayer(Tile(3221, 3218), "re_timer")
        assertTrue(timed.timers.contains("random_event"))

        Settings.load(mapOf("events.randomEvents.trigger" to "experience"))
        val untimed = createPlayer(Tile(3221, 3218), "re_no_timer")
        assertFalse(untimed.timers.contains("random_event"))
    }

    @Test
    fun `Refreshing experience without a gain doesn't start a random event`() {
        Settings.load(mapOf("events.randomEvents.chance" to "1"))
        val player = createPlayer(Tile(3221, 3218), "re_stats_refresh")
        player["random_event_cooldown"] = 1

        Skill.entries.forEach { player.experience.update(it) }
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `No random event while standing in instance space`() {
        val region = Instances.small()
        val player = createPlayer(region.tile.add(10, 10), "re_stale_instance")
        player["random_event_cooldown"] = 1

        RandomEvents.roll(player)
        tick(10)

        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `An origin inside an instance returns the player somewhere safe instead`() {
        val region = Instances.small()
        val player = createPlayer(Tile(3221, 3218), "re_instance_origin")
        player["random_event"] = "maze"
        player["random_event_origin"] = region.tile.add(10, 10).id

        RandomEvents.complete(player)

        assertFalse(Instances.reserved(player.tile.region))
        assertNull(player.get<String>("random_event"))
    }

    @Test
    fun `A new event ignores a leftover origin and returns the player to where it started`() {
        val player = createPlayer(Tile(3221, 3218), "re_stale_origin")
        player["random_event_origin"] = Tile(3460, 3721, 1).id

        RandomEvents.start(player, "maze")

        assertEquals(Tile(3221, 3218).id, player["random_event_origin", 0])
    }

    @Test
    fun `Resuming an event after logout keeps the original origin`() {
        val player = createPlayer(Tile(3221, 3218), "re_resume_origin")
        player["random_event_origin"] = Tile(3222, 3222).id

        RandomEvents.start(player, "maze", resume = true)

        assertEquals(Tile(3222, 3222).id, player["random_event_origin", 0])
    }
}
