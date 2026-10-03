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
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.removeHouseRoom
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Tables
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
    fun `Build the piece of furniture matching the hotspot`() {
        val player = createBuilder("garden")
        player.levels.set(Skill.Construction, 15)
        player.inventory.add("bagged_oak_tree", 2)
        player.enterPortal(2)
        val tree = player.objects("garden_tree_space").single()
        val bigTree = player.objects("garden_big_tree_space").single()

        player.build("garden_tree_space", "oak_tree")
        player.build("garden_big_tree_space", "oak_tree")

        assertEquals(tree.tile, player.objects("oak_tree").single().tile)
        assertEquals(bigTree.tile, player.objects("big_oak_tree").single().tile)
        assertEquals(0, player.inventory.count("bagged_oak_tree"))
    }

    @Test
    fun `Every hotspot outside of a group has furniture for its piece`() {
        for (hotspot in Tables.get("house_hotspots").rows()) {
            val furniture = hotspot.itemList("furniture")
            assertTrue(furniture.size in 1..7, hotspot.stringId)
            if (hotspot.stringOrNull("group") != null) {
                continue
            }
            for (id in furniture) {
                val objects = Tables.objList("house_furniture.$id.objects")
                val piece = hotspot.int("piece")
                val index = Tables.intListOrNull("house_furniture.$id.pieces")?.indexOf(piece) ?: piece
                assertNotNull(objects.getOrNull(index), "${hotspot.stringId} $id")
            }
        }
    }

    @Test
    fun `Build furniture which leaves some pieces empty`() {
        val player = createBuilder("combat_room")
        player.levels.set(Skill.Construction, 71)
        player.inventory.add("teak_plank", 8)
        player.enterPortal(2)

        player.build("combat_room_ring_rope_space", "ranging_pedestals")

        assertEquals(2, player.objects("ranging_spot").size)
        assertEquals(8, player.objects("magic_barrier").size)
        assertTrue(player.objects("combat_room_ring_rope_space").isEmpty())
        assertTrue(player.objects("combat_room_ring_mat_middle_space").isEmpty())
        assertEquals(listOf("combat_ring"), player.houseFurnitureHotspots)
    }

    @Test
    fun `Build furniture only on tiles with its anchors`() {
        val player = createBuilder("combat_room")
        player.levels.set(Skill.Construction, 81)
        player.inventory.add("teak_plank", 10)
        player.inventory.add("steel_bar", 5)
        player.enterPortal(2)
        val tiles = (player.objects("combat_room_ring_barrier_space_2") + player.objects("combat_room_ring_beam_space")).map { it.tile }
        val mats = player.objects("combat_room_ring_mat_middle_space").associate { it.tile to it.rotation }

        player.build("combat_room_ring_rope_space", "balance_beam")

        assertEquals(listOf(tiles.first(), tiles.last()).toSet(), player.objects("balance_beam_end").map { it.tile }.toSet())
        val middles = player.objects("balance_beam")
        assertEquals(tiles.drop(1).dropLast(1).toSet(), middles.map { it.tile }.toSet())
        assertTrue(middles.all { it.rotation == (mats.getValue(it.tile) + 1) and 0x3 })
        assertTrue(player.objects("combat_room_ring_beam_space").isEmpty())
        assertTrue(player.objects("combat_room_ring_mat_middle_space").isEmpty())
    }

    @Test
    fun `Removing anchored furniture restores its hotspots`() {
        val player = createBuilder("combat_room")
        player.addHouseFurniture(START_ROOM, "combat_ring", "balance_beam")
        player.enterPortal(2)

        player.objectOption(player.objects("balance_beam").first(), "Remove")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertTrue(player.objects("balance_beam").isEmpty())
        assertEquals(3, player.objects("combat_room_ring_beam_space").size)
        assertEquals(4, player.objects("combat_room_ring_mat_middle_space").size)
    }

    @Test
    fun `Removing furniture restores empty pieces`() {
        val player = createBuilder("combat_room")
        player.addHouseFurniture(START_ROOM, "combat_ring", "boxing_ring")
        player.enterPortal(2)
        assertTrue(player.objects("combat_room_ring_barrier_space").isEmpty())

        player.objectOption(player.objects("boxing_mat_middle").first(), "Remove")
        tickIf { player.dialogue == null }
        player.dialogueOption("line1")

        assertTrue(player.objects("boxing_ring").isEmpty())
        assertEquals(3, player.objects("combat_room_ring_barrier_space").size)
        assertEquals(4, player.objects("combat_room_ring_mat_middle_space").size)
        assertTrue(player.houseFurnitureIds.isEmpty())
    }

    @Test
    fun `Build furniture showing the players family crest`() {
        val player = createBuilder("throne_room")
        player["heraldry_crest"] = 3
        player.levels.set(Skill.Construction, 66)
        player.inventory.add("oak_plank", 2)
        player.enterPortal(2)

        player.build("throne_room_decoration_space", "round_shield")

        assertEquals(2, player.objects("round_shield_3").size)
        assertTrue(player.objects("throne_room_decoration_space").isEmpty())
    }

    @Test
    fun `Build furniture matching the house style`() {
        val player = createBuilder("throne_room")
        player["house_style"] = "basic_stone"
        player.levels.set(Skill.Construction, 61)
        player.inventory.add("mahogany_plank", 5)
        player.enterPortal(2)

        player.build("throne_room_floor_space_2", "floor_decoration")

        assertEquals(4, player.objects("floor_decoration_basic_stone").size)
        assertTrue(player.objects("throne_room_floor_space_2").isEmpty())
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

    @Test
    fun `Free building doesn't need levels, tools or materials`() {
        Settings.load(mapOf("construction.freeBuild" to "true"))
        val player = createBuilder()
        player.inventory.remove("hammer")
        player.inventory.remove("saw")
        player.enterPortal(2)

        player.build("parlour_chair_space", "mahogany_armchair")

        assertEquals(1, player.objects("mahogany_armchair").size)
        assertEquals(listOf("mahogany_armchair"), player.houseFurnitureIds)
        assertEquals(0.0, player.experience.get(Skill.Construction))
    }

    @Test
    fun `Free building builds rooms without levels or coins`() {
        Settings.load(mapOf("construction.freeBuild" to "true"))
        val player = createBuilder("garden")
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        player.objectOption(GameObjects.find(base.add(4, 4, GROUND_LEVEL).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "throne_room", "Build")
        player.dialogueOption("line3")

        assertEquals("throne_room", player.houseRoomIds.last())
    }

    @Test
    fun `Make a flatpack at a workbench`() {
        val player = createBuilder()
        player.inventory.add("plank", 2)
        player.inventory.add("bronze_nails", 2)
        val bench = createObject("wooden_workbench", exit.addY(1))

        player.objectOption(bench, "Work-at")
        tickIf { !player.hasOpen("flatpack_creation") }
        player.interfaceOption("flatpack_creation", "armchairs", "Select")
        player.interfaceOption("furniture_creation", "items", "Build", item = Item("crude_wooden_chair"), slot = 0)

        assertEquals(1, player.inventory.count("crude_wooden_chair_flatpack"))
        assertEquals(0, player.inventory.count("plank"))
        assertEquals(58.0, player.experience.get(Skill.Construction))
    }

    @Test
    fun `Can't make flatpacks better than the workbench`() {
        val player = createBuilder()
        player.levels.set(Skill.Construction, 99)
        player.inventory.add("oak_plank", 3)
        val bench = createObject("wooden_workbench", exit.addY(1))

        player.objectOption(bench, "Work-at")
        tickIf { !player.hasOpen("flatpack_creation") }
        player.interfaceOption("flatpack_creation", "armchairs", "Select")
        player.interfaceOption("furniture_creation", "items", "Build", item = Item("oak_armchair"), slot = 1)

        assertEquals(0, player.inventory.count("oak_armchair_flatpack"))
        assertEquals(3, player.inventory.count("oak_plank"))
    }

    @Test
    fun `Build furniture from a flatpack`() {
        val player = createBuilder()
        player.inventory.remove("hammer")
        player.inventory.add("crude_wooden_chair_flatpack")
        player.enterPortal(2)

        player.build("parlour_chair_space", "crude_wooden_chair")

        assertEquals(1, player.objects("crude_wooden_chair").size)
        assertEquals(0, player.inventory.count("crude_wooden_chair_flatpack"))
        assertEquals(0.0, player.experience.get(Skill.Construction))
    }

    @Test
    fun `Upgrade a workbench`() {
        val player = createBuilder("workshop")
        player.levels.set(Skill.Construction, 62)
        player.addHouseFurniture(START_ROOM, "workshop_workbench_space", "steel_framed_bench")
        player.inventory.add("oak_plank", 2)
        player.inventory.add("steel_bar", 1)
        player.enterPortal(2)

        player.objectOption(player.objects("steel_framed_bench").single(), "Upgrade")
        tickIf { !player.hasOpen("furniture_creation") }
        val menu = player.inventories.inventory("poh_furniture_menu_inv")
        assertEquals("bench_with_vice", menu[0].id)
        player.interfaceOption("furniture_creation", "items", "Build", item = Item("bench_with_vice"), slot = 0)

        assertTrue(player.objects("steel_framed_bench").isEmpty())
        assertEquals(1, player.objects("bench_with_vice").size)
        assertEquals(listOf("bench_with_vice"), player.houseFurnitureIds)
        assertEquals(0, player.inventory.count("oak_plank"))
    }

    @Test
    fun `Can't build upgrades without what they upgrade`() {
        val player = createBuilder("workshop")
        player.levels.set(Skill.Construction, 62)
        player.inventory.add("oak_plank", 2)
        player.inventory.add("steel_bar", 1)
        player.enterPortal(2)

        player.build("workshop_workbench_space", "bench_with_vice")

        assertTrue(player.objects("bench_with_vice").isEmpty())
        assertTrue(player.houseFurnitureIds.isEmpty())
    }
}
