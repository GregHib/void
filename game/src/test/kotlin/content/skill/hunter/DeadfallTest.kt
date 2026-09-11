package content.skill.hunter

import FakeRandom
import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DeadfallTest : WorldTest() {
    @ParameterizedTest
    @CsvSource(
        "wild_kebbit, kebbit_claws",
        "barb_tailed_kebbit, barb_tail_harpoon",
        "prickly_kebbit, kebbit_spike",
        "sabre_toothed_kebbit, kebbit_teeth",
    )
    fun `Catch a kebbit`(id: String, loot: String) {
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        player.inventory.add("knife")
        player.inventory.add("logs")
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(boulder, "Set-trap")
        tick(10)
        assertTrue(GameObjects.at(boulder.tile).any { it.id == "boulder_trap_setup" })
        assertEquals(0, player.inventory.count("logs"))
        assertEquals(1, player.inventory.count("knife"))
        createNPC(id, boulder.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(boulder.tile).firstOrNull { it.id == "boulder_trap_$id" }
        assertNotNull(trap)

        player.objectOption(trap, "Check")
        tick(10)
        assertEquals(1, player.inventory.count("bones"))
        assertEquals(1, player.inventory.count(loot))
        assertEquals(0, player.inventory.count("logs"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
        assertFalse(GameObjects.at(boulder.tile).any { it.id.startsWith("boulder_trap_") })
    }

    @Test
    fun `Can't set trap without a knife`() {
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        player.inventory.add("logs")
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(boulder, "Set-trap")
        tick(10)
        assertFalse(GameObjects.at(boulder.tile).any { it.id == "boulder_trap_setup" })
        assertTrue(player.containsMessage("You need a knife"))
    }

    @Test
    fun `Can't set trap without hunter level`() {
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        player.inventory.add("knife")
        player.inventory.add("logs")

        player.objectOption(boulder, "Set-trap")
        tick(10)
        assertFalse(GameObjects.at(boulder.tile).any { it.id == "boulder_trap_setup" })
    }

    @Test
    fun `Only one deadfall at a time`() {
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        val second = createObject("boulder_trap", player.tile.addX(-2))
        player.inventory.add("knife")
        player.inventory.add("logs", 2)
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(boulder, "Set-trap")
        tick(10)
        assertTrue(GameObjects.at(boulder.tile).any { it.id == "boulder_trap_setup" })

        player.objectOption(second, "Set-trap")
        tick(10)
        assertFalse(GameObjects.at(second.tile).any { it.id == "boulder_trap_setup" })
        assertTrue(player.containsMessage("only set up one deadfall"))
    }

    @Test
    fun `Fail to catch collapses the trap`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        player.inventory.add("knife")
        player.inventory.add("logs")
        player.levels.set(Skill.Hunter, 50)

        player.objectOption(boulder, "Set-trap")
        tick(10)
        assertTrue(GameObjects.at(boulder.tile).any { it.id == "boulder_trap_setup" })
        createNPC("wild_kebbit", boulder.tile.addY(2))

        tick(22)

        assertNull(GameObjects.at(boulder.tile).firstOrNull { it.id == "boulder_trap_wild_kebbit" })
        assertNull(GameObjects.at(boulder.tile).firstOrNull { it.id == "boulder_trap_setup" })
        assertEquals(0, player.inventory.count("logs"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Dismantle returns the logs`() {
        val player = createPlayer(Tile(2557, 2895))
        val boulder = createObject("boulder_trap", player.tile.addX(1))
        player.inventory.add("knife")
        player.inventory.add("logs")
        player.levels.set(Skill.Hunter, 99)

        player.objectOption(boulder, "Set-trap")
        tick(10)
        val trap = GameObjects.at(boulder.tile).firstOrNull { it.id == "boulder_trap_setup" }
        assertNotNull(trap)

        player.objectOption(trap, "Dismantle")
        tick(10)
        assertEquals(1, player.inventory.count("logs"))
        assertFalse(GameObjects.at(boulder.tile).any { it.id.startsWith("boulder_trap_") })
    }
}
