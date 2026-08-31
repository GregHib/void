package content.skill.hunter

import FakeRandom
import WorldTest
import containsMessage
import itemOnObject
import itemOption
import objectOption
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BoxTrapTest : WorldTest() {
    @ParameterizedTest
    @ValueSource(strings = ["chinchompa", "carnivorous_chinchompa"])
    fun `Catch a chinchompa`(id: String) {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })
        createNPC(id, player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "box_trap_$id" }
        assertNotNull(trap)

        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(1, player.inventory.count("box_trap"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @ParameterizedTest
    @ValueSource(strings = ["pawya", "grenwall"])
    fun `Catch with bait`(id: String) {
        val bait = if (id == "pawya") "papaya_fruit" else "raw_pawya_meat"
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.inventory.add(bait)
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        val laid = GameObjects.at(start).firstOrNull { it.id == "box_trap" }
        assertNotNull(laid)
        player.itemOnObject(laid, player.inventory.indexOf(bait))
        tick(2)
        assertEquals(0, player.inventory.count(bait))
        createNPC(id, player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "box_trap_$id" }
        assertNotNull(trap)

        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(1, player.inventory.count("box_trap"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
        if (id == "pawya") {
            assertEquals(1, player.inventory.count("raw_pawya_meat"))
        } else {
            assertEquals(18, player.inventory.count("grenwall_spikes"))
        }
    }

    @Test
    fun `Grenwall spikes stack when checking with few free slots`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.inventory.add("raw_pawya_meat")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        val laid = GameObjects.at(start).firstOrNull { it.id == "box_trap" }
        assertNotNull(laid)
        player.itemOnObject(laid, player.inventory.indexOf("raw_pawya_meat"))
        tick(2)
        player.inventory.add("bones", 26)
        assertEquals(2, player.inventory.spaces)
        createNPC("grenwall", player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "box_trap_grenwall" }
        assertNotNull(trap)
        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(18, player.inventory.count("grenwall_spikes"))
        assertEquals(1, player.inventory.count("box_trap"))
    }

    @Test
    fun `Bait is not returned when a caught trap collapses`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.inventory.add("papaya_fruit")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        val laid = GameObjects.at(start).firstOrNull { it.id == "box_trap" }
        assertNotNull(laid)
        player.itemOnObject(laid, player.inventory.indexOf("papaya_fruit"))
        tick(2)
        createNPC("pawya", player.tile.addY(2))

        tick(22)
        assertNotNull(GameObjects.at(start).firstOrNull { it.id == "box_trap_pawya" })

        tick(110)
        assertEquals(1, FloorItems.at(start).count { it.id == "box_trap" })
        assertEquals(0, FloorItems.at(start).count { it.id == "papaya_fruit" })
    }

    @Test
    fun `Grenwall ignores unbaited trap`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })
        createNPC("grenwall", player.tile.addY(2))

        tick(22)

        assertNull(GameObjects.at(start).firstOrNull { it.id == "box_trap_grenwall" })
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })
    }

    @Test
    fun `Ferret requires Eagles' Peak`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        createNPC("ferret", player.tile.addY(2))

        tick(22)

        assertNull(GameObjects.at(start).firstOrNull { it.id == "box_trap_ferret" })
    }

    @Test
    fun `Catch a ferret after Eagles' Peak`() {
        val player = createPlayer()
        val start = player.tile
        player["eagles_peak"] = "completed"
        player.inventory.add("box_trap")
        player.levels.set(Skill.Hunter, 99)

        player.itemOption("Lay", "box_trap")
        tick(3)
        createNPC("ferret", player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "box_trap_ferret" }
        assertNotNull(trap)

        player.objectOption(trap, "Check")
        tick(3)
        assertEquals(1, player.inventory.count("box_trap"))
        assertEquals(1, player.inventory.count("ferret"))
        assertNotEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't lay without hunter level`() {
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")

        player.itemOption("Lay", "box_trap")
        tick(3)
        assertFalse(GameObjects.at(start).any { it.id == "box_trap" })
        assertEquals(1, player.inventory.count("box_trap"))
    }

    @Test
    fun `Fail to catch`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 4) 0 else until - 1
        })
        val player = createPlayer()
        val start = player.tile
        player.inventory.add("box_trap")
        player.levels.set(Skill.Hunter, 60)

        player.itemOption("Lay", "box_trap")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })
        createNPC("chinchompa", player.tile.addY(2))

        tick(22)

        val trap = GameObjects.at(start).firstOrNull { it.id == "box_trap_fail" }
        assertNotNull(trap)

        player.objectOption(trap, "Dismantle")
        tick(3)
        assertEquals(1, player.inventory.count("box_trap"))
        assertEquals(0.0, player.experience.get(Skill.Hunter))
    }

    @Test
    fun `Can't place more than two traps at level 27`() {
        val player = createPlayer()
        player.levels.set(Skill.Hunter, 27)
        var start = player.tile
        player.inventory.add("box_trap", 3)

        player.itemOption("Lay", "box_trap")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })

        player.tele(start.x + 5, start.y)
        tick()
        start = player.tile
        player.itemOption("Lay", "box_trap")
        tick(3)
        assertTrue(GameObjects.at(start).any { it.id == "box_trap" })

        player.tele(start.x + 5, start.y)
        tick()
        start = player.tile
        player.itemOption("Lay", "box_trap")
        tick(3)
        assertFalse(GameObjects.at(start).any { it.id == "box_trap" })
        assertTrue(player.containsMessage("only 2 traps at a time"))
    }
}
