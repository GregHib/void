package content.skill.construction

import WorldTest
import containsMessage
import content.quest.instance
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.houseFurnitureHotspots
import content.skill.construction.House.Companion.houseFurnitureIds
import content.skill.construction.House.Companion.houseFurnitureRooms
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.removeHouseRoom
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FurnitureCreationTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)

    private fun createBuilder(room: String = "parlour"): Player {
        val player = createPlayer(exit, "builder")
        player["house_location"] = "rimmington"
        player.addHouseRoom(room, START_ROOM)
        player.inventory.add("hammer")
        player.inventory.add("saw")
        return player
    }

    private fun Player.enterPortal(option: Int) {
        objectOption(GameObjects.find(portal, "house_portal_rimmington"), "Enter")
        tickIf { dialogue == null }
        dialogueOption("line$option")
        tickIf { hasOpen("house_loading") }
    }

    private fun Player.objects(id: String): List<GameObject> {
        val zone = roomZone(instance()!!.tile.zone, START_ROOM)
        return zone.toCuboid().mapNotNull { GameObjects.findOrNull(it, id) }
    }

    private fun Player.build(hotspot: String, furniture: String) {
        objectOption(objects(hotspot).first(), "Build")
        tickIf { !hasOpen("furniture_creation") }
        val slot = inventories.inventory("poh_furniture_menu_inv").indexOf(furniture)
        interfaceOption("furniture_creation", "items", "Build", item = Item(furniture), slot = slot)
    }

    @Test
    fun `Build furniture on a hotspot`() {
        val player = createBuilder()
        player.inventory.add("plank", 2)
        player.inventory.add("bronze_nails", 2)
        player.enterPortal(2)
        val hotspot = player.objects("parlour_chair_space").single()

        player.build("parlour_chair_space", "crude_wooden_chair")

        assertFalse(player.hasOpen("furniture_creation"))
        assertEquals(hotspot.tile, player.objects("crude_wooden_chair").single().tile)
        assertTrue(player.objects("parlour_chair_space").isEmpty())
        assertEquals(0, player.inventory.count("plank"))
        assertEquals(0, player.inventory.count("bronze_nails"))
        assertEquals(58.0, player.experience.get(Skill.Construction))
        assertEquals(listOf(START_ROOM), player.houseFurnitureRooms)
        assertEquals(listOf("parlour_chair_space"), player.houseFurnitureHotspots)
        assertEquals(listOf("crude_wooden_chair"), player.houseFurnitureIds)
    }

    @Test
    fun `Build furniture in a rotated room`() {
        val player = createBuilder("garden")
        player.inventory.add("coins", 1000)
        player.inventory.add("plank", 2)
        player.inventory.add("bronze_nails", 2)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone
        player.objectOption(GameObjects.find(base.add(4, 4, GROUND_LEVEL).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "parlour", "Build")
        player.dialogueOption("line1")
        player.dialogueOption("line3")
        val parlour = player.houseRoomPositions.last()
        val zone = roomZone(base, parlour)
        val hotspot = zone.toCuboid().mapNotNull { GameObjects.findOrNull(it, "parlour_chair_space") }.single()
        player.tele(hotspot.tile.add(1, 0))

        player.objectOption(hotspot, "Build")
        tickIf { !player.hasOpen("furniture_creation") }
        player.interfaceOption("furniture_creation", "items", "Build", item = Item("crude_wooden_chair"), slot = 0)

        assertEquals(listOf(hotspot.tile), zone.toCuboid().mapNotNull { GameObjects.findOrNull(it, "crude_wooden_chair")?.tile })
    }

    @Test
    fun `Furniture menu lists a hotspots furniture down each column`() {
        val player = createBuilder()
        player.inventory.add("plank", 2)
        player.inventory.add("bronze_nails", 2)
        player.enterPortal(2)

        player.objectOption(player.objects("parlour_chair_space").single(), "Build")
        tickIf { !player.hasOpen("furniture_creation") }

        val menu = player.inventories.inventory("poh_furniture_menu_inv")
        assertEquals("crude_wooden_chair", menu[0].id)
        assertEquals("wooden_chair", menu[2].id)
        assertEquals("oak_armchair", menu[1].id)
        assertEquals("", menu[7].id)
        assertTrue(player["furniture_creation_hide_cross_1", false])
        assertFalse(player["furniture_creation_hide_cross_2", false])
        assertFalse(player["furniture_creation_hide_cross_7", false])
    }

    @Test
    fun `Empty menu slots have no cross`() {
        val player = createBuilder()
        player.enterPortal(2)
        player.objectOption(player.objects("parlour_chair_space").single(), "Build")
        tickIf { !player.hasOpen("furniture_creation") }
        player.interfaceOption("furniture_creation", "close", "Close")

        player.objectOption(player.objects("parlour_fireplace_space").single(), "Build")
        tickIf { !player.hasOpen("furniture_creation") }

        assertFalse(player["furniture_creation_hide_cross_3", false])
        for (slot in 4..7) {
            assertTrue(player["furniture_creation_hide_cross_$slot", false])
        }
    }

    @Test
    fun `Can't build furniture without the level`() {
        val player = createBuilder()
        player.inventory.add("plank", 3)
        player.inventory.add("bronze_nails", 3)
        player.enterPortal(2)

        player.build("parlour_chair_space", "wooden_chair")

        assertTrue(player.objects("wooden_chair").isEmpty())
        assertEquals(3, player.inventory.count("plank"))
        assertTrue(player.houseFurnitureIds.isEmpty())
    }

    @Test
    fun `Can't build furniture without materials`() {
        val player = createBuilder()
        player.inventory.add("plank", 2)
        player.enterPortal(2)

        player.build("parlour_chair_space", "crude_wooden_chair")

        assertTrue(player.objects("crude_wooden_chair").isEmpty())
        assertEquals(2, player.inventory.count("plank"))
        assertTrue(player.containsMessage("right materials"))
    }

    @Test
    fun `Can't build furniture without a hammer and saw`() {
        val player = createBuilder()
        player.inventory.remove("saw")
        player.inventory.add("plank", 2)
        player.inventory.add("bronze_nails", 2)
        player.enterPortal(2)

        player.build("parlour_chair_space", "crude_wooden_chair")

        assertTrue(player.objects("crude_wooden_chair").isEmpty())
        assertEquals(2, player.inventory.count("plank"))
        assertTrue(player.containsMessage("hammer and saw"))
    }

    @Test
    fun `Build every piece of grouped hotspots together`() {
        val player = createBuilder()
        player.levels.set(Skill.Construction, 2)
        player.inventory.add("bolt_of_cloth", 2)
        player.enterPortal(2)
        val corners = player.objects("parlour_rug_space_corner").map { it.tile }
        val sides = player.objects("parlour_rug_space_side").map { it.tile }
        val middles = player.objects("parlour_rug_space_middle").map { it.tile }

        player.build("parlour_rug_space_side", "brown_rug")

        assertEquals(4, corners.size)
        assertEquals(corners, player.objects("brown_rug_corner").map { it.tile })
        assertEquals(sides, player.objects("brown_rug_side").map { it.tile })
        assertEquals(middles, player.objects("brown_rug_middle").map { it.tile })
        assertTrue(player.objects("parlour_rug_space_middle").isEmpty())
        assertEquals(listOf("parlour_rug"), player.houseFurnitureHotspots)
    }

    @Test
    fun `Remove furniture`() {
        val player = createBuilder()
        player.addHouseFurniture(START_ROOM, "parlour_chair_space", "crude_wooden_chair")
        player.addHouseFurniture(START_ROOM, "parlour_chair_space_2", "crude_wooden_chair")
        player.enterPortal(2)
        val chair = player.objects("crude_wooden_chair").first { GameObjects.original(it)?.id == "parlour_chair_space" }

        player.objectOption(chair, "Remove")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertEquals(chair.tile, player.objects("parlour_chair_space").single().tile)
        assertEquals(1, player.objects("crude_wooden_chair").size)
        assertTrue(player.objects("parlour_chair_space_2").isEmpty())
        assertEquals(listOf("parlour_chair_space_2"), player.houseFurnitureHotspots)
    }

    @Test
    fun `Remove every piece of grouped furniture`() {
        val player = createBuilder()
        player.addHouseFurniture(START_ROOM, "parlour_rug", "rug")
        player.enterPortal(2)

        player.objectOption(player.objects("rug_middle").first(), "Remove")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertTrue(player.objects("rug_corner").isEmpty())
        assertTrue(player.objects("rug_side").isEmpty())
        assertTrue(player.objects("rug_middle").isEmpty())
        assertEquals(4, player.objects("parlour_rug_space_corner").size)
        assertTrue(player.houseFurnitureIds.isEmpty())
    }

    @Test
    fun `New houses start with an exit portal`() {
        val player = createPlayer(exit, "builder")
        player["house_location"] = "rimmington"

        player.enterPortal(1)

        assertEquals(listOf("exit_portal"), player.houseFurnitureIds)
        assertNotNull(player.objects("exit_portal").singleOrNull())
    }

    @Test
    fun `Can't remove the last exit portal`() {
        val player = createBuilder("garden")
        player.addHouseFurniture(START_ROOM, "garden_centrepiece_space", "exit_portal")
        player.enterPortal(2)

        player.objectOption(player.objects("exit_portal").single(), "Remove")
        tick(5)

        assertNull(player.dialogue)
        assertEquals(1, player.objects("exit_portal").size)
        assertEquals(listOf("exit_portal"), player.houseFurnitureIds)
        assertTrue(player.containsMessage("exit portal"))
    }

    @Test
    fun `Can't remove furniture outside of building mode`() {
        val player = createBuilder()
        player.addHouseFurniture(START_ROOM, "parlour_chair_space", "oak_chair")
        player.enterPortal(1)

        player.objectOption(player.objects("oak_chair").single(), "Remove")
        tick(5)

        assertNull(player.dialogue)
        assertEquals(1, player.objects("oak_chair").size)
        assertTrue(player.containsMessage("building mode"))
    }

    @Test
    fun `Furniture is placed when entering a house`() {
        val player = createBuilder("garden")
        player.addHouseFurniture(START_ROOM, "garden_centrepiece_space", "pond")

        player.enterPortal(1)

        assertNotNull(player.objects("pond").singleOrNull())
        assertTrue(player.objects("garden_centrepiece_space").isEmpty())
    }

    @Test
    fun `Removing a room removes its furniture`() {
        val player = createBuilder()
        player.addHouseRoom("garden", START_ROOM + 1)
        player.addHouseFurniture(START_ROOM, "parlour_chair_space", "oak_chair")
        player.addHouseFurniture(START_ROOM + 1, "garden_centrepiece_space", "pond")

        player.removeHouseRoom(START_ROOM)

        assertEquals(listOf(START_ROOM + 1), player.houseFurnitureRooms)
        assertEquals(listOf("pond"), player.houseFurnitureIds)
    }
}
