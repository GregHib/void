package content.minigame.vinesweeper

import WorldTest
import containsMessage
import dialogueContinue
import dialogueOption
import intEntry
import interfaceOption
import itemOnNpc
import npcOption
import objectOption
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VinesweeperTest : WorldTest() {

    // A quiet corner of the field where every tile holds a hole.
    private val corner = Tile(1610, 4690)

    @BeforeEach
    fun clearField() {
        VinesweeperField.reset()
    }

    private fun digger(tile: Tile = corner.addX(-1), name: String = "digger"): Player {
        val player = createPlayer(tile, name)
        player.inventory.add("spade")
        return player
    }

    private fun hole(tile: Tile) = VinesweeperField.hole(tile)

    private fun objectId(tile: Tile) = GameObjects.at(tile).firstOrNull { it.id.startsWith("vinesweeper") }?.id

    @Test
    fun `Field holes load from the map`() {
        assertNotNull(hole(corner))
        assertTrue(VinesweeperField.holes().size > 1000)
    }

    @Test
    fun `Digging without a spade does nothing`() {
        val player = createPlayer(corner.addX(-1))

        player.objectOption(hole(corner)!!, "Dig")
        tick(3)

        assertTrue(player.containsMessage("need a spade"))
        assertNotNull(hole(corner))
    }

    @Test
    fun `Digging next to seeds reveals their count`() {
        val player = digger()
        VinesweeperField.plant(corner.addX(1))
        VinesweeperField.plant(corner.addY(1))
        VinesweeperField.plant(corner.add(-1, -1))

        player.objectOption(hole(corner)!!, "Dig")
        tick(4)

        assertEquals("vinesweeper_number_3", objectId(corner))
        assertEquals(1, player.get("vinesweeper_points", 0))
        assertNotNull(hole(corner.addX(2)))
    }

    @Test
    fun `Digging an empty area uncovers the holes around it`() {
        val player = digger()

        player.objectOption(hole(corner)!!, "Dig")
        tick(4)

        assertEquals("vinesweeper_number_0", objectId(corner))
        assertEquals("vinesweeper_number_0", objectId(corner.add(2, 2)))
        assertEquals("vinesweeper_number_0", objectId(corner.add(-2, -2)))
        assertNotNull(hole(corner.addX(3)))
        assertEquals(25, player.get("vinesweeper_points", 0))
    }

    @Test
    fun `Digging a seed loses ten points and kills the plant`() {
        val player = digger()
        player["vinesweeper_points"] = 15
        VinesweeperField.plant(corner)

        player.objectOption(hole(corner)!!, "Dig")
        tick(4)

        assertEquals("vinesweeper_dead_plant", objectId(corner))
        assertEquals(5, player.get("vinesweeper_points", 0))
        assertFalse(VinesweeperField.isSeed(corner))
        assertTrue(player.containsMessage("potato seed"))
    }

    @Test
    fun `Points never drop below zero`() {
        val player = digger()
        player["vinesweeper_points"] = 3
        VinesweeperField.plant(corner)

        player.objectOption(hole(corner)!!, "Dig")
        tick(4)

        assertEquals(0, player.get("vinesweeper_points", 0))
    }

    @Test
    fun `Planting a flag uses one up and marks the hole`() {
        val player = digger()
        player.inventory.add("flag", 3)

        player.objectOption(hole(corner)!!, "Flag")
        tick(4)

        assertEquals("vinesweeper_flag", objectId(corner))
        assertEquals(2, player.inventory.count("flag"))
        assertEquals(player.accountName, VinesweeperField.flagOwner(corner))
    }

    @Test
    fun `Farmer finds a seed under a flag and rewards the player`() {
        val player = digger()
        player.levels.set(Skill.Farming, 10)
        player.inventory.add("flag")
        VinesweeperField.plant(corner)
        createNPC("farmer_minesweeper", corner.add(3, 0))

        player.objectOption(hole(corner)!!, "Flag")
        tickIf { objectId(corner) != "vinesweeper_flag" }
        tickIf(40) { VinesweeperField.flagOwner(corner) != null }
        tick(8)

        val points = player.get("vinesweeper_points", 0)
        assertTrue(points in 10..40, "Expected level based points, got $points")
        assertEquals(1, player.inventory.count("flag"))
        assertNotNull(hole(corner))
        assertTrue(VinesweeperField.seedCount > 0) // the farmer replants the field after clearing
    }

    @Test
    fun `Farmer keeps a flag planted on an empty hole`() {
        val player = digger()
        player.inventory.add("flag")
        createNPC("farmer_minesweeper", corner.add(3, 0))

        player.objectOption(hole(corner)!!, "Flag")
        tickIf { objectId(corner) != "vinesweeper_flag" }
        tickIf(40) { VinesweeperField.flagOwner(corner) != null }
        tick(8)

        assertEquals(0, player.get("vinesweeper_points", 0))
        assertEquals(0, player.inventory.count("flag"))
        assertNotNull(hole(corner))
    }

    @Test
    fun `Rabbit eats a flagged seed before the farmer arrives`() {
        val player = digger()
        player.inventory.add("flag")
        VinesweeperField.plant(corner)
        createNPC("rabbit_minesweeper", corner.add(1, 0))
        createNPC("farmer_minesweeper", corner.add(12, 0))

        player.objectOption(hole(corner)!!, "Flag")
        tickIf { objectId(corner) != "vinesweeper_flag" }
        tickIf(20) { objectId(corner) == "vinesweeper_flag" }

        assertEquals("vinesweeper_dead_plant", objectId(corner))
        assertFalse(VinesweeperField.isSeed(corner))
        assertEquals(0, player.get("vinesweeper_points", 0))
    }

    @Test
    fun `Farmer clears dug up seeds and the numbers around them`() {
        val player = digger()
        VinesweeperField.plant(corner)
        val farmer = createNPC("farmer_minesweeper", corner.add(4, 0))
        player.objectOption(hole(corner.add(-1, 0))!!, "Dig") // reveals a 1
        tick(4)
        assertEquals("vinesweeper_number_1", objectId(corner.add(-1, 0)))

        player.objectOption(hole(corner)!!, "Dig")
        tickIf(40) { farmer.tile != corner }
        tick(8)

        assertNotNull(hole(corner))
        assertNotNull(hole(corner.add(-1, 0)))
    }

    @Test
    fun `Entering the field shows the points overlay and leaving refunds ogleroots`() {
        val player = createPlayer(Tile(1600, 4700))
        player.inventory.add("ogleroot", 4)

        player.tele(corner)
        tick(2)
        assertTrue(player.interfaces.contains("vinesweeper_points"))

        player.tele(Tile(3052, 3304))
        tick(2)
        assertFalse(player.interfaces.contains("vinesweeper_points"))
        assertEquals(0, player.inventory.count("ogleroot"))
        assertEquals(40, player.inventory.count("coins"))
    }

    @Test
    fun `Feeding a rabbit an ogleroot gives hunter experience and hides it`() {
        val player = digger()
        player.inventory.add("ogleroot")
        val rabbit = createNPC("rabbit_minesweeper", corner)

        player.itemOnNpc(rabbit, player.inventory.indexOf("ogleroot"))
        tick(6)

        assertEquals(0, player.inventory.count("ogleroot"))
        assertEquals(30.0, player.experience.get(Skill.Hunter))
        assertTrue(rabbit.hide)
    }

    @Test
    fun `First ten flags are free then they cost coins`() {
        val player = digger()
        val winkin = createNPC("mrs__winkin_minesweeper", corner.addX(1))

        player.npcOption(winkin, "Buy flags")
        tick(2)
        assertEquals(10, player.inventory.count("flag"))
        player.dialogueContinue()

        player.inventory.remove("flag", 4)
        player.inventory.add("coins", 2000)
        player.npcOption(winkin, "Buy flags")
        tick(2)
        player.dialogueContinue()
        tick()
        player.intEntry(3)
        tick()

        assertEquals(9, player.inventory.count("flag"))
        assertEquals(500, player.inventory.count("coins"))
    }

    @Test
    fun `Reward shop sells seeds for points`() {
        val player = digger()
        player["vinesweeper_points"] = 25
        player.open("vinesweeper_rewards")
        tick()

        player.interfaceOption("vinesweeper_rewards", "items", "Buy 1", slot = 3 * 5) // tomato seed, 10 points
        tick()
        assertEquals(1, player.inventory.count("tomato_seed"))
        assertEquals(15, player.get("vinesweeper_points", 0))

        player.interfaceOption("vinesweeper_rewards", "items", "Buy 1", slot = 18 * 5) // torstol seed, 45000 points
        tick()
        assertEquals(0, player.inventory.count("torstol_seed"))
        assertTrue(player.containsMessage("enough points"))
    }

    @Test
    fun `Trading points for experience uses the level rate`() {
        val player = digger()
        player["vinesweeper_points"] = 1000
        player.levels.set(Skill.Farming, 1)
        player.open("vinesweeper_rewards")
        tick()

        player.interfaceOption("vinesweeper_rewards", "items", "Value", slot = 0)
        tick()
        player.interfaceOption("vinesweeper_rewards", "proceed", "Proceed")
        tick()

        assertEquals(130.0, player.experience.get(Skill.Farming))
        assertEquals(0, player.get("vinesweeper_points", 0))

        player["vinesweeper_points"] = 1000
        player.levels.set(Skill.Farming, 40)
        player.interfaceOption("vinesweeper_rewards", "items", "Value", slot = 0)
        tick()
        player.interfaceOption("vinesweeper_rewards", "proceed", "Proceed")
        tick()
        assertEquals(1130.0, player.experience.get(Skill.Farming))
    }

    @Test
    fun `Leprechaun teleports to the farm and the portal sends you back`() {
        val player = createPlayer(Tile(3229, 3229))
        val leprechaun = createNPC("tool_leprechaun", Tile(3229, 3230))

        player.npcOption(leprechaun, "Teleport")
        tick(2)
        player.dialogueContinue()
        tick()
        player.dialogueOption("line1") // Yes please.
        tick()
        val origin = player.tile
        player.skipDialogues()
        tick(2)
        assertEquals(origin, player.tile) // the projectile lands and the impact graphic plays first
        tick(4)

        assertEquals(Vinesweeper.ARRIVAL_TILE, player.tile)
        assertEquals(origin.id, player.get("vinesweeper_return_tile", -1))

        val portal = GameObjects.find(Tile(1637, 4710), "vinesweeper_portal")
        player.objectOption(portal, "Enter")
        tick(4)
        assertEquals(origin, player.tile)
    }
}
