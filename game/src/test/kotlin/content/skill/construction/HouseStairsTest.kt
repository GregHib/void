package content.skill.construction

import WorldTest
import containsMessage
import content.quest.instance
import content.skill.construction.House.Companion.DUNGEON_LEVEL
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.UPPER_LEVEL
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.houseRoomRotations
import content.skill.construction.House.Companion.houseStairs
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import content.skill.construction.House.Companion.stairsDown
import dialogueOption
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.map.zone.DynamicZones.Companion.rotatedId
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseStairsTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)
    private val hall = roomPosition(4, 3, GROUND_LEVEL)
    private val upperHall = roomPosition(4, 3, UPPER_LEVEL)
    private val dungeon = roomPosition(4, 3, DUNGEON_LEVEL)

    private fun createOwner(): Player {
        val player = createPlayer(exit, "owner")
        player["house_location"] = "rimmington"
        player.levels.set(Skill.Construction, 99)
        player.addHouseRoom("garden", START_ROOM)
        player.addHouseFurniture(START_ROOM, "garden_centrepiece_space", "exit_portal")
        return player
    }

    private fun Player.enterPortal(option: Int) {
        objectOption(GameObjects.find(portal, "house_portal_rimmington"), "Enter")
        tickIf { dialogue == null }
        dialogueOption("line$option")
        tickIf { hasOpen("house_loading") }
    }

    private fun Player.find(position: Int, id: String): GameObject {
        val zone = roomZone(instance()!!.tile.zone, position)
        return zone.toCuboid().firstNotNullOf { GameObjects.findOrNull(it, id) }
    }

    private fun Player.findOrNull(position: Int, id: String): GameObject? {
        val zone = roomZone(instance()!!.tile.zone, position)
        return zone.toCuboid().firstNotNullOfOrNull { GameObjects.findOrNull(it, id) }
    }

    /**
     * Uses [option] on [obj], waiting long enough to walk around it
     */
    private fun Player.use(obj: GameObject, option: String) {
        tele(obj.tile.addY(-1))
        objectOption(obj, option)
        tick(8)
    }

    private fun Player.addEntrance() {
        addHouseRoom("garden", hall)
        addHouseFurniture(hall, "garden_centrepiece_space", "dungeon_entrance")
    }

    private fun Player.addHalls() {
        addHouseRoom("skill_hall", hall)
        addHouseFurniture(hall, "skill_hall_stair_space", "oak_staircase")
        addHouseRoom("skill_hall", upperHall)
        addHouseFurniture(upperHall, "skill_hall_stair_space_down", "oak_staircase")
    }

    private fun Player.addDungeonStairs() {
        addHouseRoom("skill_hall", hall)
        addHouseFurniture(hall, "skill_hall_stair_space_down", "oak_staircase")
        addHouseRoom("dungeon_stairs", dungeon)
        addHouseFurniture(dungeon, "skill_hall_stair_space", "oak_staircase")
    }

    private fun Player.buildStairs() {
        inventory.add("hammer")
        inventory.add("saw")
        inventory.add("oak_plank", 10)
        inventory.add("steel_bar", 4)
        use(find(hall, "skill_hall_stair_space"), "Build")
        tickIf { !hasOpen("furniture_creation") }
        interfaceOption("furniture_creation", "items", "Build", item = Item("oak_staircase"), slot = 0)
    }

    @Test
    fun `Dungeon entrance without a room below doesn't lead anywhere`() {
        val player = createOwner()
        player.addEntrance()
        player.enterPortal(1)
        val entrance = player.find(hall, "dungeon_entrance")

        player.use(entrance, "Enter")

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertEquals("dialogue_message2", player.dialogue)
    }

    @Test
    fun `Enter the dungeon below the entrance`() {
        val player = createOwner()
        player.addEntrance()
        player.addHouseRoom("dungeon_stairs", dungeon)
        player.enterPortal(1)
        val entrance = player.find(hall, "dungeon_entrance")

        player.use(entrance, "Enter")

        assertEquals(DUNGEON_LEVEL, player.tile.level)
        assertEquals(roomZone(player.instance()!!.tile.zone, dungeon), player.tile.zone)
        assertFalse(player.hasOpen("house_loading"))
    }

    @Test
    fun `Build a dungeon room through the entrance in building mode`() {
        val player = createOwner()
        player.addEntrance()
        player.inventory.add("coins", 7500)
        player.enterPortal(2)
        val entrance = player.find(hall, "dungeon_entrance")

        player.use(entrance, "Enter")
        player.skipDialogues()
        player.dialogueOption("line1")
        tick(4)

        assertEquals("dungeon_stairs", player.houseRoomIds.last())
        assertEquals(dungeon, player.houseRoomPositions.last())
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(DUNGEON_LEVEL, player.tile.level)
        assertFalse(player.hasOpen("house_loading"))
        assertEquals("oak_staircase", player.houseStairs(dungeon))
        assertFalse(player.stairsDown(dungeon))
        player.find(dungeon, "oak_staircase")
    }

    @Test
    fun `Building a dungeon entrance adds stairs to the dungeon room below`() {
        val player = createOwner()
        player.addHouseRoom("garden", hall)
        player.addHouseRoom("dungeon_stairs", dungeon)
        player.inventory.add("hammer")
        player.inventory.add("saw")
        player.inventory.add("marble_block")
        player.enterPortal(2)

        player.use(player.find(hall, "garden_centrepiece_space"), "Build")
        tickIf { !player.hasOpen("furniture_creation") }
        player.interfaceOption("furniture_creation", "items", "Build", item = Item("dungeon_entrance"), slot = 1)

        assertEquals("oak_staircase", player.houseStairs(dungeon))
        player.find(hall, "dungeon_entrance")
        player.find(dungeon, "oak_staircase")
    }

    @Test
    fun `Declining to build below the entrance stays put`() {
        val player = createOwner()
        player.addEntrance()
        player.inventory.add("coins", 7500)
        player.enterPortal(2)
        val entrance = player.find(hall, "dungeon_entrance")

        player.use(entrance, "Enter")
        player.skipDialogues()
        player.dialogueOption("line4")

        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
        assertEquals(7500, player.inventory.count("coins"))
        assertEquals(GROUND_LEVEL, player.tile.level)
    }

    @Test
    fun `Climb up and down stairs between halls`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(1)

        player.use(player.find(hall, "oak_staircase"), "Climb-up")

        assertEquals(UPPER_LEVEL, player.tile.level)
        assertEquals(roomZone(player.instance()!!.tile.zone, upperHall), player.tile.zone)

        player.use(player.find(upperHall, "oak_staircase_down"), "Climb-down")

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertFalse(player.hasOpen("house_loading"))
    }

    @Test
    fun `Climb down to the dungeon stairs and back up`() {
        val player = createOwner()
        player.addDungeonStairs()
        player.enterPortal(1)

        player.use(player.find(hall, "oak_staircase_down"), "Climb-down")
        tick(4)

        assertEquals(DUNGEON_LEVEL, player.tile.level)
        assertFalse(player.hasOpen("house_loading"))

        player.use(player.find(dungeon, "oak_staircase"), "Climb-up")

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertFalse(player.hasOpen("house_loading"))
    }

    @Test
    fun `Stairs without a room above don't lead anywhere`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseFurniture(hall, "skill_hall_stair_space", "oak_staircase")
        player.enterPortal(1)

        player.use(player.find(hall, "oak_staircase"), "Climb-up")

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertEquals("dialogue_message2", player.dialogue)
        player.skipDialogues()
        assertNull(player.dialogue)
    }

    @Test
    fun `Build a hall at the top of the stairs in building mode`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall, rotation = 2)
        player.addHouseFurniture(hall, "skill_hall_stair_space", "oak_staircase")
        player.inventory.add("coins", 25000)
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        player.skipDialogues()
        player.dialogueOption("line2")

        assertEquals("quest_hall", player.houseRoomIds.last())
        assertEquals(upperHall, player.houseRoomPositions.last())
        assertEquals(2, player.houseRoomRotations.last())
        assertEquals("oak_staircase", player.houseStairs(upperHall))
        assertEquals(UPPER_LEVEL, player.tile.level)
        player.find(upperHall, "oak_staircase_down")
    }

    @Test
    fun `Building stairs builds them on the floor above`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseRoom("skill_hall", upperHall)
        player.enterPortal(2)

        player.buildStairs()
        player.dialogueOption("line1")

        assertEquals("oak_staircase", player.houseStairs(hall))
        assertFalse(player.stairsDown(hall))
        assertEquals("oak_staircase", player.houseStairs(upperHall))
        assertEquals(0, player.inventory.count("oak_plank"))
        player.find(hall, "oak_staircase")
        player.find(upperHall, "oak_staircase_down")
    }

    @Test
    fun `Build stairs leading down to the dungeon stairs`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseRoom("dungeon_stairs", dungeon)
        player.enterPortal(2)

        player.buildStairs()
        player.dialogueOption("line2")

        assertTrue(player.stairsDown(hall))
        assertEquals("oak_staircase", player.houseStairs(dungeon))
        assertEquals(0, player.inventory.count("oak_plank"))
        player.find(hall, "oak_staircase_down")
        player.find(dungeon, "oak_staircase")
    }

    @Test
    fun `Build a dungeon stairs room at the bottom of the stairs in building mode`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseFurniture(hall, "skill_hall_stair_space_down", "oak_staircase")
        player.inventory.add("coins", 7500)
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase_down"), "Climb-down")
        player.skipDialogues()
        player.dialogueOption("line1")
        tick(4)

        assertEquals("dungeon_stairs", player.houseRoomIds.last())
        assertEquals("oak_staircase", player.houseStairs(dungeon))
        assertEquals(DUNGEON_LEVEL, player.tile.level)
    }

    @Test
    fun `Removing stairs leading down restores the stair space`() {
        val player = createOwner()
        player.addDungeonStairs()
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase_down"), "Remove")
        player.dialogueOption("line1")

        assertNull(player.houseStairs(hall))
        assertNull(player.houseStairs(dungeon))
        player.find(hall, "skill_hall_stair_space")
        player.find(dungeon, "skill_hall_stair_space")
    }

    @Test
    fun `Removing stairs removes them from both floors`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(2)

        player.use(player.find(upperHall, "oak_staircase_down"), "Remove")
        player.dialogueOption("line1")

        assertNull(player.houseStairs(hall))
        assertNull(player.houseStairs(upperHall))
        assertNull(player.findOrNull(hall, "oak_staircase"))
        player.find(hall, "skill_hall_stair_space")
    }

    @Test
    fun `Cancel building a room at the top of the stairs`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseFurniture(hall, "skill_hall_stair_space", "oak_staircase")
        player.inventory.add("coins", 25000)
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        player.skipDialogues()
        player.dialogueOption("line4")

        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
        assertEquals(25000, player.inventory.count("coins"))
        assertEquals(GROUND_LEVEL, player.tile.level)
    }

    @Test
    fun `Can't build an upstairs room without a room below to support it`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(2)
        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        val upper = roomZone(player.instance()!!.tile.zone, upperHall).tile
        player.tele(upper.add(6, 3))

        player.objectOption(GameObjects.find(upper.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tick(5)

        assertFalse(player.hasOpen("room_creation"))
        assertEquals("dialogue_message1", player.dialogue)
        assertNull(player["house_preview_position"])
    }

    @Test
    fun `Build an upstairs room with a room below to support it`() {
        val player = createOwner()
        player.addHalls()
        player.addHouseRoom("parlour", roomPosition(5, 3, GROUND_LEVEL))
        player.enterPortal(2)
        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        val upper = roomZone(player.instance()!!.tile.zone, upperHall).tile
        player.tele(upper.add(6, 3))

        player.objectOption(GameObjects.find(upper.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tick(5)

        assertTrue(player.hasOpen("room_creation"))
        assertEquals(roomPosition(5, 3, UPPER_LEVEL), player["house_preview_position"])
    }

    @Test
    fun `Can't build an upstairs room above a garden`() {
        val player = createOwner()
        player.addHalls()
        player.addHouseRoom("garden", roomPosition(5, 3, GROUND_LEVEL))
        player.enterPortal(2)
        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        val upper = roomZone(player.instance()!!.tile.zone, upperHall).tile
        player.tele(upper.add(6, 3))

        player.objectOption(GameObjects.find(upper.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tick(5)

        assertFalse(player.hasOpen("room_creation"))
        assertEquals("dialogue_message1", player.dialogue)
        assertNull(player["house_preview_position"])
    }

    @Test
    fun `Can't build gardens upstairs`() {
        val player = createOwner()
        player.addHalls()
        player.addHouseRoom("parlour", roomPosition(5, 3, GROUND_LEVEL))
        player.inventory.add("coins", 100000)
        player.enterPortal(2)
        player.use(player.find(hall, "oak_staircase"), "Climb-up")
        val upper = roomZone(player.instance()!!.tile.zone, upperHall).tile
        player.tele(upper.add(6, 3))
        player.objectOption(GameObjects.find(upper.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }

        for (garden in listOf("garden", "menagerie", "formal_garden")) {
            player.interfaceOption("room_creation", garden, "Build")
            player.open("room_creation")
        }

        assertNull(player["house_preview_room"])
        assertEquals(100000, player.inventory.count("coins"))
        assertTrue(player.containsMessage("That room can only be built on the ground floor."))
    }

    @Test
    fun `Removing other furniture keeps the stairs`() {
        val player = createOwner()
        player.addHalls()
        player.addHouseFurniture(upperHall, "skill_hall_fishing_trophy_space", "mounted_bass")
        player.enterPortal(2)

        player.use(player.find(upperHall, "mounted_bass"), "Remove")
        player.dialogueOption("line1")

        assertEquals("oak_staircase", player.houseStairs(hall))
        assertEquals("oak_staircase", player.houseStairs(upperHall))
        player.find(hall, "oak_staircase")
    }

    @Test
    fun `Can only remove rooms through stairs in building mode`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(1)

        player.use(player.find(hall, "oak_staircase"), "Remove-room")

        assertEquals("dialogue_message1", player.dialogue)
        assertEquals(listOf(START_ROOM, hall, upperHall), player.houseRoomPositions)
    }

    @Test
    fun `Remove the room above through the stairs`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase"), "Remove-room")
        player.skipDialogues()
        player.dialogueOption("line1")

        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
        assertNull(player.houseStairs(hall))
        assertNull(player.houseStairs(upperHall))
        assertNull(player.findOrNull(hall, "oak_staircase"))
    }

    @Test
    fun `Declining to remove the room through the stairs keeps it`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(2)

        player.use(player.find(upperHall, "oak_staircase_down"), "Remove-room")
        player.skipDialogues()
        player.dialogueOption("line2")

        assertEquals(listOf(START_ROOM, hall, upperHall), player.houseRoomPositions)
        assertEquals("oak_staircase", player.houseStairs(hall))
    }

    @Test
    fun `Remove the dungeon room below the entrance`() {
        val player = createOwner()
        player.addEntrance()
        player.addHouseRoom("dungeon_corridor", dungeon)
        player.enterPortal(2)

        player.use(player.find(hall, "dungeon_entrance"), "Remove-room")
        player.dialogueOption("line1")

        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
        player.find(hall, "dungeon_entrance")
    }

    @Test
    fun `No room below the entrance to remove`() {
        val player = createOwner()
        player.addEntrance()
        player.enterPortal(2)

        player.use(player.find(hall, "dungeon_entrance"), "Remove-room")

        assertEquals("dialogue_message1", player.dialogue)
        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
    }

    @Test
    fun `Entrance doesn't lead to a dungeon room without stairs`() {
        val player = createOwner()
        player.addEntrance()
        player.addHouseRoom("dungeon_corridor", dungeon)
        player.enterPortal(1)

        player.use(player.find(hall, "dungeon_entrance"), "Enter")

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertEquals("dialogue_message1", player.dialogue)
    }

    @Test
    fun `Only a dungeon stairs room is offered below the entrance`() {
        val player = createOwner()
        player.addEntrance()
        player.inventory.add("coins", 7500)
        player.enterPortal(2)

        player.use(player.find(hall, "dungeon_entrance"), "Enter")
        player.skipDialogues()

        assertEquals("dialogue_multi2", player.dialogue)
    }

    @Test
    fun `Climb up the dungeon stairs to the entrance`() {
        val player = createOwner()
        player.addEntrance()
        player.addHouseRoom("dungeon_stairs", dungeon)
        player.addHouseFurniture(dungeon, "skill_hall_stair_space", "oak_staircase")
        player.enterPortal(1)
        player.use(player.find(hall, "dungeon_entrance"), "Enter")
        tick(4)

        player.use(player.find(dungeon, "oak_staircase"), "Climb-up")
        tick(4)

        assertEquals(GROUND_LEVEL, player.tile.level)
        assertEquals(roomZone(player.instance()!!.tile.zone, hall), player.tile.zone)
    }

    @Test
    fun `Only a dungeon stairs room is offered below the stairs`() {
        val player = createOwner()
        player.addHouseRoom("skill_hall", hall)
        player.addHouseFurniture(hall, "skill_hall_stair_space_down", "oak_staircase")
        player.inventory.add("coins", 25000)
        player.enterPortal(2)

        player.use(player.find(hall, "oak_staircase_down"), "Climb-down")
        player.skipDialogues()
        player.dialogueOption("line2")

        assertEquals(listOf(START_ROOM, hall), player.houseRoomPositions)
        assertEquals(25000, player.inventory.count("coins"))
    }

    @Test
    fun `Empty dungeon covers the same area as the grass`() {
        val player = createOwner()
        player.addDungeonStairs()
        player.enterPortal(1)
        val base = player.instance()!!.tile.zone
        val dynamicZones: DynamicZones = get()
        val empty = roomZone(base, roomPosition(3, 3, DUNGEON_LEVEL))

        assertEquals(Tables.tile("house_spaces.dungeon.template").zone.rotatedId(0), dynamicZones.dynamicZone(empty))
        // Same area as the grass, one space around the ground floor rooms at x 3..4
        assertNotNull(dynamicZones.dynamicZone(roomZone(base, roomPosition(2, 3, DUNGEON_LEVEL))))
        assertNull(dynamicZones.dynamicZone(roomZone(base, roomPosition(1, 3, DUNGEON_LEVEL))))
    }

    @Test
    fun `Grass surrounds dungeon rooms beyond the ground floor`() {
        val player = createOwner()
        player.addHouseRoom("dungeon_corridor", roomPosition(5, 3, DUNGEON_LEVEL))
        player.enterPortal(1)
        val base = player.instance()!!.tile.zone
        val dynamicZones: DynamicZones = get()
        val dungeon = Tables.tile("house_spaces.dungeon.template").zone.rotatedId(0)

        // Garden at x 3 and dungeon at x 5 cover x 2..6 on both floors
        assertNotNull(dynamicZones.dynamicZone(roomZone(base, roomPosition(6, 3, GROUND_LEVEL))))
        assertNull(dynamicZones.dynamicZone(roomZone(base, roomPosition(1, 3, GROUND_LEVEL))))
        assertEquals(dungeon, dynamicZones.dynamicZone(roomZone(base, roomPosition(2, 3, DUNGEON_LEVEL))))
        assertEquals(dungeon, dynamicZones.dynamicZone(roomZone(base, roomPosition(6, 3, DUNGEON_LEVEL))))
    }

    @Test
    fun `Empty dungeon spaces show build mode template in building mode`() {
        val player = createOwner()
        player.addDungeonStairs()
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone
        val dynamicZones: DynamicZones = get()
        val empty = roomZone(base, roomPosition(3, 3, DUNGEON_LEVEL))

        assertEquals(Tables.tile("house_spaces.dungeon.build").zone.rotatedId(0), dynamicZones.dynamicZone(empty))
    }

    @Test
    fun `Dungeon spaces are left empty without a dungeon`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(1)
        val base = player.instance()!!.tile.zone
        val dynamicZones: DynamicZones = get()

        assertNull(dynamicZones.dynamicZone(roomZone(base, roomPosition(3, 3, DUNGEON_LEVEL))))
    }

    @Test
    fun `Upstairs doorways without a room on the other side are walled`() {
        val player = createOwner()
        player.addHalls()
        player.addHouseRoom("parlour", roomPosition(5, 3, GROUND_LEVEL))
        player.addHouseRoom("parlour", roomPosition(5, 3, UPPER_LEVEL))
        player.enterPortal(1)
        val base = player.instance()!!.tile.zone
        val upper = roomZone(base, upperHall).tile
        val ground = roomZone(base, hall).tile

        // Connected to the parlour to the east
        assertNull(GameObjects.findOrNull(upper.add(7, 3), "basic_wood_wall"))
        assertNull(GameObjects.findOrNull(upper.add(7, 4), "basic_wood_wall"))
        // Open to the roofs on the other sides
        for (tile in listOf(upper.add(0, 3), upper.add(0, 4), upper.add(3, 0), upper.add(4, 0), upper.add(3, 7), upper.add(4, 7))) {
            assertNotNull(GameObjects.findOrNull(tile, "basic_wood_wall"), tile.toString())
        }
        assertTrue(roomZone(base, upperHall).toCuboid().none { tile -> GameObjects.at(tile).any { it.id.startsWith("door_hotspot") } })
        // Ground floor doorways still lead outside
        assertNull(GameObjects.findOrNull(ground.add(0, 3), "basic_wood_wall"))
    }

    @Test
    fun `Upstairs doorways keep their hotspots in building mode`() {
        val player = createOwner()
        player.addHalls()
        player.enterPortal(2)
        val upper = roomZone(player.instance()!!.tile.zone, upperHall).tile

        assertNull(GameObjects.findOrNull(upper.add(0, 3), "basic_wood_wall"))
        assertNotNull(GameObjects.findOrNull(upper.add(0, 3)) { it.id.startsWith("door_hotspot") })
    }
}
