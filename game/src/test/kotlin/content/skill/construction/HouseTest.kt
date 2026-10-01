package content.skill.construction

import WorldTest
import containsMessage
import content.quest.instance
import content.skill.construction.House.Companion.DUNGEON_LEVEL
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.houseRoomIds
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.houseRoomRotations
import content.skill.construction.House.Companion.leaveHouse
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import interfaceOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import walk
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.VariableDefinition.Companion.persist
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)
    private val dynamicZones: DynamicZones
        get() = get()

    private fun createOwner(name: String = "owner"): Player {
        val player = createPlayer(exit, name)
        player["house_location"] = "rimmington"
        player.addHouseRoom("garden", START_ROOM)
        player.addHouseFurniture(START_ROOM, "garden_centrepiece_space", "exit_portal")
        return player
    }

    private fun Player.buildDoor(x: Int, y: Int, direction: Direction) {
        val base = instance()!!.tile.zone.add(x, y, GROUND_LEVEL).tile
        val tile = when (direction) {
            Direction.EAST -> base.add(7, 3)
            Direction.WEST -> base.add(0, 3)
            Direction.NORTH -> base.add(3, 7)
            else -> base.add(3, 0)
        }
        tele(base.add(3, 3))
        objectOption(GameObjects.find(tile) { it.id.startsWith("door_hotspot") }, "Build")
        tick(10)
    }

    private fun Player.enterPortal(option: Int, obj: GameObject = GameObjects.find(portal, "house_portal_rimmington")) {
        objectOption(obj, "Enter")
        tickIf { dialogue == null }
        dialogueOption("line$option")
        tickIf { hasOpen("house_loading") }
    }

    private fun Player.visit(owner: Player, obj: GameObject = GameObjects.find(portal, "house_portal_rimmington")) {
        enterPortal(1, obj)
        (suspension as Suspension.NameEntry).resume(owner.name)
        tickIf { hasOpen("house_loading") }
    }

    private fun Player.inHouseOf(owner: Player): Boolean {
        val instance = owner.instance() ?: return false
        return instance() == instance && Instances.owner(tile) == instance
    }

    @Test
    fun `Buy a house from an estate agent`() {
        val player = createPlayer(Tile(2983, 3370))
        player.inventory.add("coins", 1000)
        val agent = createNPC("estate_agent", Tile(2983, 3369))

        player.npcOption(agent, "Talk-to")
        tickIf { player.dialogue == null }
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()
        player.dialogueOption("line1")
        player.skipDialogues()

        assertEquals("rimmington", player["house_location", ""])
        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(listOf(START_ROOM), player.houseRoomPositions)
        assertEquals(listOf(0), player.houseRoomRotations)
        assertEquals(0, player.inventory.count("coins"))
    }

    private fun Player.talkToAgent(vararg options: Int) {
        val agent = createNPC("estate_agent", tile.addY(-1))
        npcOption(agent, "Talk-to")
        tickIf { dialogue == null }
        for (option in options) {
            skipDialogues()
            dialogueOption("line$option")
        }
        skipDialogues()
    }

    @Test
    fun `Move house to another location`() {
        val player = createOwner()
        player.tele(2983, 3370)
        player.levels.set(Skill.Construction, 10)
        player.inventory.add("coins", 5000)

        player.talkToAgent(1, 2)

        assertEquals("taverley", player["house_location", ""])
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Can't move house without the level`() {
        val player = createOwner()
        player.tele(2983, 3370)
        player.inventory.add("coins", 25000)

        player.talkToAgent(1, 5, 2)

        assertEquals("rimmington", player["house_location", ""])
        assertEquals(25000, player.inventory.count("coins"))
    }

    @Test
    fun `Redecorate house`() {
        val player = createOwner()
        player.tele(2983, 3370)
        player.levels.set(Skill.Construction, 50)
        player.inventory.add("coins", 25000)

        player.talkToAgent(2, 5, 2)

        assertEquals("fancy_stone", player["house_style", ""])
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Houses load in every style`() {
        for (style in Tables.get("house_styles").rows()) {
            val player = createOwner(style.rowId)
            player["house_style"] = style.rowId
            player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))

            player.enterPortal(1)

            val parlour = roomZone(player.instance()!!.tile.zone, roomPosition(4, 3, GROUND_LEVEL)).toCuboid()
            assertEquals(8, parlour.count { GameObjects.findOrNull(it, style.obj("window")) != null }, style.rowId)
            assertTrue(parlour.none { GameObjects.findOrNull(it, "house_window_space") != null }, style.rowId)
            assertTrue(player.inHouseOf(player), style.rowId)
            player.leaveHouse()
        }
    }

    @Test
    fun `Enter and leave houses through every location's portal`() {
        for (location in Tables.get("house_locations").rows()) {
            val exit = location.tile("exit")
            val owner = createOwner("owner${location.id}")
            owner["house_location"] = location.rowId
            owner.tele(exit)
            val portal = exit.toCuboid(radius = 8).firstNotNullOf { GameObjects.findOrNull(it, "house_portal_${location.rowId}") }

            owner.enterPortal(1, portal)
            val guest = createPlayer(exit, "guest${location.id}")
            guest.visit(owner, portal)

            assertTrue(owner.inHouseOf(owner), location.rowId)
            assertTrue(guest.inHouseOf(owner), location.rowId)
            guest.objectOption(guest.exitPortal(), "Enter")
            tick(5)
            assertEquals(exit, guest.tile, location.rowId)
            owner.objectOption(owner.exitPortal(), "Enter")
            tick(5)
            assertEquals(exit, owner.tile, location.rowId)
        }
    }

    @Test
    fun `Room positions cover the 7x7x4 grid`() {
        val base = Zone(800, 0)
        assertEquals(0, roomPosition(0, 0, 0))
        assertEquals(195, roomPosition(6, 6, 3))
        for (position in 0..195) {
            assertEquals(position, roomPosition(base, roomZone(base, position)))
        }
        assertNull(roomPosition(base, base))
        assertNull(roomPosition(base, base.add(8, 1)))
    }

    @Test
    fun `Enter own house`() {
        val player = createOwner()

        player.enterPortal(1)

        val instance = player.instance()
        assertNotNull(instance)
        assertEquals(instance, Instances.owner(player.tile))
        assertEquals(GROUND_LEVEL, player.tile.level)
        assertEquals(instance.tile.zone.add(4, 4, GROUND_LEVEL).tile.add(3, 2), player.tile)
        assertNull(dynamicZones.dynamicZone(instance.tile.zone.add(4, 4, DUNGEON_LEVEL)))
        assertNull(GameObjects.findOrNull(instance.tile.zone.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") })
    }

    @Test
    fun `Entering a house with missing rooms starts a new house`() {
        val player = createPlayer(exit, "owner")
        player["house_location"] = "rimmington"

        player.enterPortal(1)

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(listOf(START_ROOM), player.houseRoomPositions)
        assertEquals(listOf(0), player.houseRoomRotations)
        assertEquals(player.instance()!!.tile.zone.add(4, 4, GROUND_LEVEL).tile.add(3, 2), player.tile)
    }

    @Test
    fun `Entering a house with mismatched rooms starts a new house`() {
        val player = createOwner()
        player["house_room_rotations"] = emptyList<Int>()

        player.enterPortal(1)

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(listOf(0), player.houseRoomRotations)
    }

    @Test
    fun `Teleport to house spell`() {
        val player = createOwner()
        player["house_build_mode"] = true
        player.levels.set(Skill.Magic, 40)
        player.inventory.add("law_rune")
        player.inventory.add("earth_rune")
        player.inventory.add("air_rune")

        player.interfaceOption("modern_spellbook", "teleport_to_house", "Cast")
        tickIf { player.instance() == null }

        assertTrue(player.inventory.isEmpty())
        assertFalse(player["house_build_mode", false])
        assertTrue(player.inHouseOf(player))
        assertEquals(player.instance()!!.tile.zone.add(4, 4, GROUND_LEVEL).tile.add(3, 2), player.tile)
    }

    @Test
    fun `Teleport to house tablet`() {
        val player = createOwner()
        player["house_build_mode"] = true
        player.inventory.add("teleport_to_house")

        player.interfaceOption("inventory", "inventory", "Break", 0, Item("teleport_to_house"), 0)
        tickIf { player.instance() == null }

        assertTrue(player.inventory.isEmpty())
        assertFalse(player["house_build_mode", false])
        assertTrue(player.inHouseOf(player))
        assertEquals(player.instance()!!.tile.zone.add(4, 4, GROUND_LEVEL).tile.add(3, 2), player.tile)
    }

    @Test
    fun `Teleporting home from inside your house expels guests`() {
        val owner = createOwner()
        owner.enterPortal(2)
        val guest = createPlayer(exit, "guest")
        owner["house_build_mode"] = false
        guest.visit(owner)
        val previous = owner.instance()
        owner.inventory.add("teleport_to_house")

        owner.interfaceOption("inventory", "inventory", "Break", 0, Item("teleport_to_house"), 0)
        tickIf { owner.instance() == previous }
        tick()

        assertTrue(owner.inHouseOf(owner))
        assertEquals(exit, guest.tile)
    }

    @Test
    fun `Can't teleport to house without one`() {
        val player = createPlayer(exit)
        player.inventory.add("teleport_to_house")

        player.interfaceOption("inventory", "inventory", "Break", 0, Item("teleport_to_house"), 0)
        tick(5)

        assertEquals(1, player.inventory.count("teleport_to_house"))
        assertEquals(exit, player.tile)
        assertTrue(player.containsMessage("don't have a house"))
    }

    @Test
    fun `Build a room in building mode`() {
        val player = createOwner()
        player.levels.set(Skill.Construction, 1)
        player.inventory.add("coins", 1000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        val door = GameObjects.find(base.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }
        player.objectOption(door, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "parlour", "Build")
        player.dialogueOption("line1")
        player.dialogueOption("line3")

        assertEquals(listOf("garden", "parlour"), player.houseRoomIds)
        assertEquals(listOf(START_ROOM, roomPosition(4, 3, GROUND_LEVEL)), player.houseRoomPositions)
        assertEquals(listOf(0, 1), player.houseRoomRotations)
        assertEquals(0, player.inventory.count("coins"))
    }

    @Test
    fun `Cancelling a room preview doesn't build`() {
        val player = createOwner()
        player.inventory.add("coins", 1000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        player.objectOption(GameObjects.find(base.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "parlour", "Build")
        player.dialogueOption("line4")

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(1000, player.inventory.count("coins"))
    }

    @Test
    fun `Can't build a room without the level`() {
        val player = createOwner()
        player.inventory.add("coins", 10000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        player.objectOption(GameObjects.find(base.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "kitchen", "Build")

        assertTrue(player.containsMessage("Construction level of 5"))
        assertEquals(1, player.houseRoomIds.size)
    }

    @Test
    fun `Walking away from a room preview doesn't build`() {
        val player = createOwner()
        player.inventory.add("coins", 1000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone
        val empty = dynamicZones.dynamicZone(base.add(5, 4, GROUND_LEVEL))

        player.objectOption(GameObjects.find(base.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "parlour", "Build")
        assertNotEquals(empty, dynamicZones.dynamicZone(base.add(5, 4, GROUND_LEVEL)))
        player.walk(player.tile.addX(-1))
        tick()

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(1000, player.inventory.count("coins"))
        assertEquals(empty, dynamicZones.dynamicZone(base.add(5, 4, GROUND_LEVEL)))
        assertNull(player.get<String>("house_preview_room"))
    }

    @Test
    fun `Level 1 can only build within a 3x3 area`() {
        val player = createOwner()
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(5, 4, Direction.EAST)

        assertFalse(player.hasOpen("room_creation"))
        assertTrue(player.containsMessage("Your house is already at the maximum width."))
    }

    @Test
    fun `Can't build higher than the level allows`() {
        val player = createOwner()
        player.addHouseRoom("garden", roomPosition(3, 4, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(4, 5, Direction.NORTH)

        assertFalse(player.hasOpen("room_creation"))
        assertTrue(player.containsMessage("Your house is already at the maximum height."))
    }

    @Test
    fun `Grass surrounds the rooms`() {
        val player = createOwner()
        player.levels.set(Skill.Construction, 1)
        player.inventory.add("coins", 1000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        for (x in 3..5) {
            for (y in 3..5) {
                assertNotNull(dynamicZones.dynamicZone(base.add(x, y, GROUND_LEVEL)))
            }
        }
        assertNull(dynamicZones.dynamicZone(base.add(6, 4, GROUND_LEVEL)))
        assertNull(dynamicZones.dynamicZone(base.add(2, 4, GROUND_LEVEL)))

        player.buildDoor(4, 4, Direction.EAST)
        player.interfaceOption("room_creation", "parlour", "Build")
        player.dialogueOption("line3")

        assertNotNull(dynamicZones.dynamicZone(base.add(6, 3, GROUND_LEVEL)))
        assertNotNull(dynamicZones.dynamicZone(base.add(6, 5, GROUND_LEVEL)))
        assertNull(dynamicZones.dynamicZone(base.add(7, 4, GROUND_LEVEL)))
    }

    @Test
    fun `Remove a room through its door`() {
        val player = createOwner()
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        player.buildDoor(4, 4, Direction.EAST)
        assertFalse(player.hasOpen("room_creation"))
        player.dialogueOption("line1")

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(listOf(START_ROOM), player.houseRoomPositions)
        assertEquals(listOf(0), player.houseRoomRotations)
        assertTrue(player.containsMessage("Room deleted!"))
        assertNull(dynamicZones.dynamicZone(base.add(6, 4, GROUND_LEVEL)))
    }

    @Test
    fun `Doors from outside a room remove that room`() {
        val player = createOwner()
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)
        val parlour = player.instance()!!.tile.zone.add(5, 4, GROUND_LEVEL).tile

        player.tele(parlour.add(8, 3))
        player.objectOption(GameObjects.find(parlour.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tick(5)
        player.dialogueOption("line1")

        assertEquals(listOf("garden"), player.houseRoomIds)
    }

    @Test
    fun `Can't remove the room with the last exit portal`() {
        val player = createOwner()
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)
        val parlour = player.instance()!!.tile.zone.add(5, 4, GROUND_LEVEL).tile

        player.tele(parlour.add(0, 3))
        player.objectOption(GameObjects.find(parlour.add(-1, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tick(5)

        assertNull(player.dialogue)
        assertTrue(player.containsMessage("exit portal"))
        assertEquals(listOf("garden", "parlour"), player.houseRoomIds)
    }

    @Test
    fun `Remove a garden when another remains`() {
        val player = createOwner()
        player.addHouseRoom("garden", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(4, 4, Direction.EAST)
        player.dialogueOption("line1")

        assertEquals(listOf("garden"), player.houseRoomIds)
        assertEquals(listOf(START_ROOM), player.houseRoomPositions)
    }

    @Test
    fun `Arrive in the garden even if it isn't the first room`() {
        val player = createPlayer(exit, "owner")
        player["house_location"] = "rimmington"
        player.addHouseRoom("parlour", START_ROOM)
        player.addHouseRoom("garden", roomPosition(4, 3, GROUND_LEVEL))

        player.enterPortal(1)

        assertEquals(player.instance()!!.tile.zone.add(5, 4, GROUND_LEVEL).tile.add(3, 3), player.tile)
    }

    @Test
    fun `Choosing not to remove a room keeps it`() {
        val player = createOwner()
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(4, 4, Direction.EAST)
        player.dialogueOption("line2")

        assertEquals(listOf("garden", "parlour"), player.houseRoomIds)
    }

    @Test
    fun `Level 15 can build two rooms north and east but only one south and west`() {
        val player = createOwner()
        player.levels.set(Skill.Construction, 15)
        player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))
        player.addHouseRoom("kitchen", roomPosition(2, 3, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(3, 4, Direction.WEST)
        assertFalse(player.hasOpen("room_creation"))
        assertTrue(player.containsMessage("maximum width"))

        player.buildDoor(5, 4, Direction.EAST)
        assertTrue(player.hasOpen("room_creation"))
    }

    @Test
    fun `Level 60 can build the full 7x7 area`() {
        val player = createOwner()
        player.levels.set(Skill.Construction, 60)
        player.addHouseRoom("parlour", roomPosition(1, 3, GROUND_LEVEL))
        player.enterPortal(2)

        player.buildDoor(2, 4, Direction.WEST)

        assertTrue(player.hasOpen("room_creation"))
    }

    @Test
    fun `Can't build more rooms than the level allows`() {
        val player = createOwner()
        for (x in 0 until 7) {
            for (y in 0 until 7) {
                if (player.houseRoomIds.size < 21) {
                    player.addHouseRoom("parlour", roomPosition(x, y, DUNGEON_LEVEL))
                }
            }
        }
        player.enterPortal(2)

        player.buildDoor(4, 4, Direction.EAST)
        assertFalse(player.hasOpen("room_creation"))
        assertTrue(player.containsMessage("any more rooms"))

        player.levels.set(Skill.Construction, 38)
        player.buildDoor(4, 4, Direction.EAST)
        assertTrue(player.hasOpen("room_creation"))
    }

    @Test
    fun `Can't build dungeon rooms above ground`() {
        val player = createOwner()
        player.levels.set(Skill.Construction, 99)
        player.inventory.add("coins", 10000)
        player.enterPortal(2)
        val base = player.instance()!!.tile.zone

        player.objectOption(GameObjects.find(base.add(4, 4, 1).tile.add(7, 3)) { it.id.startsWith("door_hotspot") }, "Build")
        tickIf { !player.hasOpen("room_creation") }
        player.interfaceOption("room_creation", "dungeon_corridor", "Build")

        assertTrue(player.containsMessage("can only be built underground"))
        assertEquals(1, player.houseRoomIds.size)
    }

    @Test
    fun `Can't open house options outside of a house`() {
        val player = createOwner()

        player.interfaceOption("options", "house", "Open House Options")

        assertFalse(player.hasOpen("house_options"))
    }

    @Test
    fun `House options close on leaving house`() {
        val player = createOwner()
        player.enterPortal(1)

        player.interfaceOption("options", "house", "Open House Options")
        assertTrue(player.hasOpen("house_options"))
        player.tele(3222, 3218)
        tick()

        assertFalse(player.hasOpen("house_options"))
        assertTrue(player.hasOpen("options"))
    }

    @Test
    fun `Visit and leave another players house`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")

        guest.visit(owner)
        assertTrue(guest.inHouseOf(owner))

        guest.open("house_options")
        guest.interfaceOption("house_options", "leave_house", "Leave house")
        tick()

        assertEquals(exit, guest.tile)
        assertNull(guest.instance())
        assertTrue(owner.inHouseOf(owner))
    }

    @Test
    fun `Can't visit a house in building mode`() {
        val owner = createOwner()
        owner.enterPortal(2)
        val guest = createPlayer(exit, "guest")

        guest.visit(owner)

        assertFalse(guest.inHouseOf(owner))
        assertTrue(guest.containsMessage("build mode"))
    }

    @Test
    fun `Expel guests`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        owner.open("house_options")
        owner.interfaceOption("house_options", "expel_guests", "Expel guests")
        tick()

        assertEquals(exit, guest.tile)
        assertTrue(owner.inHouseOf(owner))
    }

    private fun Player.exitPortal(): GameObject {
        val zone = roomZone(instance()!!.tile.zone, START_ROOM)
        return zone.toCuboid().firstNotNullOf { GameObjects.findOrNull(it, "exit_portal") }
    }

    @Test
    fun `Window spaces are filled with windows`() {
        for (option in 1..2) {
            val player = createOwner("owner$option")
            player.addHouseRoom("parlour", roomPosition(4, 3, GROUND_LEVEL))

            player.enterPortal(option)

            val parlour = roomZone(player.instance()!!.tile.zone, roomPosition(4, 3, GROUND_LEVEL)).toCuboid()
            assertTrue(parlour.none { GameObjects.findOrNull(it, "house_window_space") != null })
            assertEquals(8, parlour.count { GameObjects.findOrNull(it, "basic_wood_window") != null })
        }
    }

    @Test
    fun `Leave a house through the exit portal`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        guest.objectOption(guest.exitPortal(), "Enter")
        tick(5)

        assertEquals(exit, guest.tile)
        assertNull(guest.instance())
        assertTrue(owner.inHouseOf(owner))
    }

    @Test
    fun `Locked houses can't be visited`() {
        val owner = createOwner()
        owner.enterPortal(1)

        owner.objectOption(owner.exitPortal(), "Lock")
        tick(5)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        assertTrue(owner["house_locked", false])
        assertTrue(VariableDefinitions.get("house_locked").persist)
        assertEquals(exit, guest.tile)
        assertTrue(guest.containsMessage("locked"))
    }

    @Test
    fun `Unlock a house`() {
        val owner = createOwner()
        owner["house_locked"] = true
        owner.enterPortal(1)

        owner.objectOption(owner.exitPortal(), "Lock")
        tick(5)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        assertFalse(owner["house_locked", false])
        assertTrue(guest.inHouseOf(owner))
    }

    @Test
    fun `Guests can't lock a house`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        guest.objectOption(guest.exitPortal(), "Lock")
        tick(5)

        assertFalse(owner["house_locked", false])
        assertFalse(guest.contains("house_locked"))
    }

    @Test
    fun `Building mode expels guests`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        owner.open("house_options")
        owner.interfaceOption("house_options", "building_mode_on", "Building mode on")
        tick()

        assertEquals(exit, guest.tile)
        assertTrue(owner["house_build_mode", false])
        assertTrue(owner.inHouseOf(owner))
    }

    @Test
    fun `Owner teleporting away kicks guests`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        owner.tele(3222, 3218)
        tick()

        assertNull(owner.instance())
        assertNull(owner.get<String>("house_owner"))
        assertEquals(exit, guest.tile)
    }

    @Test
    fun `Owner logging out kicks guests`() {
        val owner = createOwner()
        owner.enterPortal(1)
        val guest = createPlayer(exit, "guest")
        guest.visit(owner)

        Despawn.player(owner)
        tick()

        assertEquals(exit, owner.tile)
        assertEquals(exit, guest.tile)
    }
}
