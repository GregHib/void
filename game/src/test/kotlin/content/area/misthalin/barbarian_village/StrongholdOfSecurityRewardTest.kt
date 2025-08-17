package content.area.misthalin.barbarian_village

import WorldTest
import content.entity.player.dialogue.continueDialogue
import intEntry
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StrongholdOfSecurityRewardTest : WorldTest() {

    @Test
    fun `Get rewards from peace chest`() {
        val player = createPlayer(Tile(1907, 5223))
        val chest = objects[Tile(1907, 5222), "gift_of_peace"]!!

        player.objectOption(chest, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player["unlocked_emote_flap", false])
        assertEquals(2000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim twice from peace chest`() {
        val player = createPlayer(Tile(1907, 5223))
        player["unlocked_emote_flap"] = true
        val chest = objects[Tile(1907, 5222), "gift_of_peace"]!!

        player.objectOption(chest, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim with full inventory from peace chest`() {
        val player = createPlayer(Tile(1907, 5223))
        player.inventory.add("shark", 28)
        val chest = objects[Tile(1907, 5222), "gift_of_peace"]!!

        player.objectOption(chest, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertFalse(player["unlocked_emote_flap", false])
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Get rewards from grain of plenty`() {
        val player = createPlayer(Tile(2021, 5214))
        val grain = objects[Tile(2021, 5215), "grain_of_plenty"]!!

        player.objectOption(grain, "Search")
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player["unlocked_emote_slap_head", false])
        assertEquals(3000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim twice from grain of plenty`() {
        val player = createPlayer(Tile(2021, 5214))
        player["unlocked_emote_flap"] = true
        val grain = objects[Tile(2021, 5215), "grain_of_plenty"]!!

        player.objectOption(grain, "Search")
        tick()
        player.continueDialogue()
        tick()

        assertEquals(3000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim with full inventory from grain of plenty`() {
        val player = createPlayer(Tile(2021, 5214))
        player.inventory.add("shark", 28)
        val grain = objects[Tile(2021, 5215), "grain_of_plenty"]!!

        player.objectOption(grain, "Search")
        tick()
        player.continueDialogue()
        tick()

        assertFalse(player["unlocked_emote_slap_head", false])
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Get rewards from box of health`() {
        val player = createPlayer(Tile(2144, 5281))
        player.experience.set(Skill.Prayer, Level.experience(15))
        player.levels.set(Skill.Prayer, 10)
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)
        val box = objects[Tile(2144, 5280), "box_of_health"]!!

        player.objectOption(box, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player["unlocked_emote_idea", false])
        assertEquals(15, player.levels.get(Skill.Prayer))
        assertEquals(150, player.levels.get(Skill.Constitution))
        assertEquals(5000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim twice from box of health`() {
        val player = createPlayer(Tile(2144, 5281))
        player["unlocked_emote_flap"] = true
        player.experience.set(Skill.Prayer, Level.experience(15))
        player.levels.set(Skill.Prayer, 10)
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)
        val box = objects[Tile(2144, 5280), "box_of_health"]!!

        player.objectOption(box, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player["unlocked_emote_idea", false])
        assertEquals(15, player.levels.get(Skill.Prayer))
        assertEquals(150, player.levels.get(Skill.Constitution))
        assertEquals(5000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't claim with full inventory from box of health`() {
        val player = createPlayer(Tile(2144, 5281))
        player.inventory.add("shark", 28)
        player.experience.set(Skill.Prayer, Level.experience(15))
        player.levels.set(Skill.Prayer, 10)
        player.experience.set(Skill.Constitution, Level.experience(15))
        player.levels.set(Skill.Constitution, 100)
        val box = objects[Tile(2144, 5280), "box_of_health"]!!

        player.objectOption(box, "Open")
        tick()
        player.continueDialogue()
        tick()

        assertFalse(player["unlocked_emote_idea", false])
        assertEquals(10, player.levels.get(Skill.Prayer))
        assertEquals(100, player.levels.get(Skill.Constitution))
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Get rewards from cradle of life`() {
        val player = createPlayer(Tile(2343, 5214))
        val cradle = objects[Tile(2344, 5214), "cradle_of_life"]!!

        player.objectOption(cradle, "Search")
        tick()
        repeat(4) {
            player.continueDialogue()
            tick()
        }
        player.intEntry(1)
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player["unlocked_emote_stomp", false])
        assertTrue(player.inventory.contains("fancy_boots"))
    }

    @Test
    fun `Can replace lost items from cradle of life`() {
        val player = createPlayer(Tile(2343, 5214))
        player["unlocked_emote_stomp"] = true
        val cradle = objects[Tile(2344, 5214), "cradle_of_life"]!!

        player.objectOption(cradle, "Search")
        tick()
        repeat(5) {
            player.continueDialogue()
            tick()
        }
        player.intEntry(2)
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player.inventory.contains("fighting_boots"))
    }

    @Test
    fun `Can exchange rewards from cradle of life`() {
        val player = createPlayer(Tile(2343, 5214))
        player["unlocked_emote_stomp"] = true
        player.inventory.add("fancy_boots")
        val cradle = objects[Tile(2344, 5214), "cradle_of_life"]!!

        player.objectOption(cradle, "Search")
        tick()
        player.continueDialogue()
        tick()
        player.intEntry(1)
        tick()
        player.continueDialogue()
        tick()

        assertTrue(player.inventory.contains("fighting_boots"))
    }
}
