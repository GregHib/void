package content.skill.construction

import WorldTest
import content.quest.instance
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import objectOption
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseSeatsTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)
    private val parlour = roomPosition(4, 3, GROUND_LEVEL)

    private fun createOwner(): Player {
        val player = createPlayer(exit, "owner")
        player["house_location"] = "rimmington"
        player.levels.set(Skill.Construction, 99)
        player.addHouseRoom("garden", START_ROOM)
        player.addHouseFurniture(START_ROOM, "garden_centrepiece_space", "exit_portal")
        player.addHouseRoom("parlour", parlour)
        player.addHouseFurniture(parlour, "parlour_chair_space", "oak_chair")
        return player
    }

    private fun Player.enterHouse() {
        objectOption(GameObjects.find(portal, "house_portal_rimmington"), "Enter")
        tickIf { dialogue == null }
        dialogueOption("line1")
        tickIf { hasOpen("house_loading") }
    }

    private fun Player.findOrNull(id: String): GameObject? {
        val zone = roomZone(instance()!!.tile.zone, parlour)
        return zone.toCuboid().firstNotNullOfOrNull { GameObjects.findOrNull(it, id) }
    }

    @Test
    fun `Sit on a chair hides it until walking away`() {
        val player = createOwner()
        player.enterHouse()
        val chair = player.findOrNull("oak_chair")!!

        player.objectOption(chair, "Sit-on")
        tick(10)

        assertEquals(chair.tile, player.tile)
        assertNull(player.findOrNull("oak_chair"))
        assertTrue(player.contains("house_seat"))

        player.walk(chair.tile.addX(2))
        tick(5)

        assertNotEquals(chair.tile, player.tile)
        assertFalse(player.contains("house_seat"))
        assertEquals(chair, player.findOrNull("oak_chair"))
    }

    @Test
    fun `Stopping sitting puts the chair back`() {
        val player = createOwner()
        player.enterHouse()
        val chair = player.findOrNull("oak_chair")!!
        player.objectOption(chair, "Sit-on")
        tick(10)

        player.softTimers.stop("house_seat")

        assertFalse(player.contains("house_seat"))
        assertEquals(chair, player.findOrNull("oak_chair"))
    }

    @Test
    fun `Sit on a dining bench and get up again`() {
        val player = createOwner()
        val dining = roomPosition(3, 3, GROUND_LEVEL)
        player.addHouseRoom("dining_room", dining)
        player.addHouseFurniture(dining, "dining_room_bench_space", "wooden_bench")
        player.enterHouse()
        val zone = roomZone(player.instance()!!.tile.zone, dining)
        val bench = zone.toCuboid().firstNotNullOf { GameObjects.findOrNull(it, "wooden_bench") }

        player.objectOption(bench, "Sit-on")
        tick(10)

        assertEquals(bench.tile, player.tile)
        assertEquals("invisible_seat", GameObjects.getLayer(bench.tile, ObjectLayer.layer(bench.shape))?.id)

        player.walk(bench.tile.addY(-3))
        tick(5)

        assertFalse(player.contains("house_seat"))
        assertEquals(bench, GameObjects.findOrNull(bench.tile, "wooden_bench"))
    }

    @Test
    fun `Sit on a dining bench a second time`() {
        val player = createOwner()
        val dining = roomPosition(3, 3, GROUND_LEVEL)
        player.addHouseRoom("dining_room", dining)
        player.addHouseFurniture(dining, "dining_room_bench_space", "wooden_bench")
        player.enterHouse()
        val zone = roomZone(player.instance()!!.tile.zone, dining)
        val bench = zone.toCuboid().firstNotNullOf { GameObjects.findOrNull(it, "wooden_bench") }
        player.objectOption(bench, "Sit-on")
        tick(10)
        player.walk(bench.tile.addY(-3))
        tick(5)

        player.objectOption(bench, "Sit-on")
        tick(10)

        assertEquals(bench.tile, player.tile)
        assertTrue(player.contains("house_seat"))
    }

    @Test
    fun `Throne room benches aren't pulled out`() {
        val player = createOwner()
        val throneRoom = roomPosition(3, 3, GROUND_LEVEL)
        player.addHouseRoom("throne_room", throneRoom)
        player.addHouseFurniture(throneRoom, "throne_room_seating_space", "carved_teak_bench")
        player.enterHouse()
        val zone = roomZone(player.instance()!!.tile.zone, throneRoom)
        val bench = zone.toCuboid().firstNotNullOf { GameObjects.findOrNull(it, "carved_teak_bench_throne_room") }
        val benches = zone.toCuboid().count { GameObjects.findOrNull(it, "carved_teak_bench_throne_room") != null }

        player.objectOption(bench, "Sit-on")
        tickIf { !player.contains("house_seat") }

        assertEquals(bench.tile, player.tile)
        // Only the sat on bench is hidden without a pushed back copy appearing
        assertEquals(benches - 1, zone.toCuboid().count { GameObjects.findOrNull(it, "carved_teak_bench_throne_room") != null })

        player.walk(bench.tile.addY(-3))
        tick()

        assertFalse(player.contains("house_seat"))
        assertEquals(bench, GameObjects.findOrNull(bench.tile, "carved_teak_bench_throne_room"))
    }
}
