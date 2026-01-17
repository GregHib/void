package content.activity.penguin_hide_and_seek

import WorldTest
import containsMessage
import content.entity.effect.transform
import dialogueContinue
import dialogueOption
import interfaceOption
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import itemOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.koin.core.component.get
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PenguinHideAndSeekTest : WorldTest() {

    @Test
    fun `Talk with larry`() {
        val player = createPlayer(Tile(2597, 3266))
        val larry = createNPC("larry_ardougne", Tile(2596, 3266))

        player.npcOption(larry, "Talk-to")
        tick()

        player.dialogueContinue(24)
        assertTrue(player["penguin_hide_and_seek_explained", false])
        player.dialogueContinue(1)
        assertTrue(player.inventory.contains("spy_notebook"))
    }

    @Test
    fun `Spot a penguin gives points`() {
        val player = createPlayer(Tile(2599, 3267))
        val penguin = createNPC("hidden_penguin_5", Tile(2599, 3271))
        penguin.transform("crate_penguin", collision = false)

        player.npcOption(penguin, "Spy-on")
        tick()

        assertEquals(1, player["penguin_points", 0])
        assertEquals(1, player["penguins_found_weekly", 0])
        assertFalse(player.containsVarbit("penguins_found", "penguin_0"))
        assertTrue(player.containsVarbit("penguins_found", "penguin_5"))
    }

    @Test
    fun `Spot a two point penguin`() {
        val player = createPlayer(Tile(2599, 3267))
        player["cold_war"] = "completed"
        val penguin = createNPC("hidden_penguin_6", Tile(2599, 3271))
        penguin.transform("crate_penguin", collision = false)

        player.npcOption(penguin, "Spy-on")
        tick()

        assertEquals(2, player["penguin_points", 0])
        assertEquals(1, player["penguins_found_weekly", 0])
        assertTrue(player.containsVarbit("penguins_found", "penguin_6"))
    }

    @Test
    fun `Spot a polar bear`() {
        val player = createPlayer(Tile(2599, 3267))
        val bear = createObject("polar_bear_well_ardougne", Tile(2599, 3271))

        player.objectOption(bear, "Inspect")
        tick()

        assertEquals(1, player["penguin_points", 1])
        assertEquals(1, player["penguins_found_weekly", 1])
        assertTrue(player.containsVarbit("penguins_found", "polar_bear"))
    }

    @Test
    fun `Trade in points for coins`() {
        val player = createPlayer(Tile(2597, 3266))
        player.levels.set(Skill.Thieving, 35)
        player["penguin_hide_and_seek_explained"] = true
        player["penguin_points"] = 4
        val larry = createNPC("larry_ardougne", Tile(2596, 3266))

        player.npcOption(larry, "Hide-n-Seek")
        tick()

        player.dialogueContinue(2)
        player.dialogueOption("line1")
        player.dialogueContinue()
        assertEquals(26000, player.inventory.count("coins"))
    }

    @Test
    fun `Trade in points for experience`() {
        val player = createPlayer(Tile(2597, 3266))
        player.levels.set(Skill.Thieving, 35)
        player["penguin_hide_and_seek_explained"] = true
        player["penguin_points"] = 4
        val larry = createNPC("larry_ardougne", Tile(2596, 3266))

        player.npcOption(larry, "Hide-n-Seek")
        tick()

        player.dialogueContinue(2)
        player.dialogueOption("line2")
        player.dialogueContinue()
        player.interfaceOption("skill_stat_advance", "thieving", "Select", optionIndex = 0)
        player.interfaceOption("skill_stat_advance", "confirm", "Confirm")
        assertEquals(3500.0, player.experience.get(Skill.Thieving))
    }

    @Test
    fun `Check points with spy handbook`() {
        val player = createPlayer(Tile(2597, 3266))
        player["penguin_hide_and_seek_explained"] = true
        player["penguin_points"] = 3
        player["penguins_found_weekly"] = 2
        val larry = createNPC("larry_ardougne", Tile(2596, 3266))

        player.npcOption(larry, "Talk-to")
        tick()

        mockkStatic("content.entity.player.dialogue.DialogueCommonKt")
        val interfaces = spyk(player.interfaces)
        player.interfaces = interfaces
        player.dialogueContinue(4)
        player.itemOption("Read", "spy_notebook")
        assertTrue(player.containsMessage("You have recently spotted 2 penguins."))
        assertFalse(player.containsMessage("polar bear agent"))
        assertTrue(player.containsMessage("You have 3 Penguin Points to spend with Larry."))
        player.addVarbit("penguins_found", "polar_bear")
        player.itemOption("Read", "spy_notebook")
        assertTrue(player.containsMessage("You have recently spotted the polar bear agent."))
    }

    @Test
    fun `Ask for location hint`() {
        val player = createPlayer(Tile(2597, 3266))
        player["penguin_hide_and_seek_explained"] = true
        player.inventory.add("spy_notebook")
        createNPC("hidden_penguin_0", Tile(2596, 3265))
        val larry = createNPC("larry_ardougne", Tile(2596, 3266))

        player.npcOption(larry, "Talk-to")
        tick()

        mockkStatic("content.entity.player.dialogue.DialogueCommonKt")
        val interfaces = spyk(player.interfaces)
        player.interfaces = interfaces
        player.dialogueContinue()
        player.dialogueOption("line2")
        player.dialogueContinue(2)
        verify {
            interfaces.sendText("dialogue_npc_chat1", "line1", "I've heard there's a penguin located around Ardougne.")
        }
    }

    @Test
    fun `Ticks until midnight`() {
        val day = DayOfWeek.THURSDAY
        val now = ZonedDateTime.of(2025, 9, 24, 21, 0, 0, 0, ZoneOffset.UTC)
        val instance = instance()
        val ticks = instance.ticksUntil(day, now)
        assertEquals(18_000, ticks) // 3 hours
    }

    @Test
    fun `Ticks until midnight next week`() {
        val day = DayOfWeek.THURSDAY
        val now = ZonedDateTime.of(2025, 9, 25, 0, 0, 0, 0, ZoneOffset.UTC)
        val instance = instance()
        val ticks = instance.ticksUntil(day, now)
        assertEquals(1_008_000, ticks) // 1 week
    }

    @Test
    fun `Seasonal disguises`() {
        val instance = instance()
        assertEquals("crate_penguin", instance.disguise("crate", LocalDate.of(2025, 9, 25)))
        assertEquals("pumpkin_penguin", instance.disguise("rock", LocalDate.of(2025, 10, 29)))
        assertEquals("toadstool_turkey", instance.disguise("toadstool", LocalDate.of(2025, 11, 29)))
        assertEquals("snowman_penguin", instance.disguise("toadstool", LocalDate.of(2025, 12, 1)))
    }

    @Test
    fun `Weeks since epoch is stable`() {
        val now = ZonedDateTime.of(2025, 9, 25, 0, 0, 0, 0, ZoneOffset.UTC)
        val instance = instance()
        assertEquals(889, instance.weeksSince(DayOfWeek.FRIDAY, now))
        assertEquals(890, instance.weeksSince(DayOfWeek.TUESDAY, now))
        assertEquals(890, instance.weeksSince(DayOfWeek.THURSDAY, now))
    }

    @Test
    fun `Spawn npcs`() {
        val player = createPlayer()
        player["hunt_for_red_rektuber"] = "completed"
        Settings.load(mapOf("events.penguinHideAndSeek.enabled" to "true"))
        val instance = instance()
        instance.load(configFiles)
        tick()
        for (penguin in instance.penguins) {
            assertNotNull(penguin)
        }
        instance.sendBear()
        assertNotEquals("hidden", instance.bear)
        assertEquals(instance.bear, player["polar_bear_well", "hidden"])
        // Clear
        instance.clear()
        for (penguin in instance.penguins) {
            assertNull(penguin)
        }
        assertEquals("hidden", instance.bear)
        assertEquals("hidden", player["polar_bear_well", "hidden"])
    }

    private fun instance(): PenguinHideAndSeek {
        val instance = PenguinHideAndSeek(get(), get())
        return instance
    }
}
