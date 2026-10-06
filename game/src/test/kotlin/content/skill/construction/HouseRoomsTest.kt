package content.skill.construction

import WorldTest
import containsMessage
import content.quest.instance
import content.quest.joinInstance
import content.skill.construction.House.Companion.DUNGEON_LEVEL
import content.skill.construction.House.Companion.GROUND_LEVEL
import content.skill.construction.House.Companion.START_ROOM
import content.skill.construction.House.Companion.addHouseFurniture
import content.skill.construction.House.Companion.addHouseRoom
import content.skill.construction.House.Companion.houseFurnitureIds
import content.skill.construction.House.Companion.leaveHouse
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.roomZone
import dialogueOption
import intEntry
import interfaceOption
import itemOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HouseRoomsTest : WorldTest() {

    private val portal = Tile(2951, 3222)
    private val exit = Tile(2953, 3224)
    private val room = roomPosition(4, 3, GROUND_LEVEL)
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

    private fun Player.objects(position: Int, id: String): List<GameObject> {
        val zone = roomZone(instance()!!.tile.zone, position)
        return zone.toCuboid().mapNotNull { GameObjects.findOrNull(it, id) }
    }

    /**
     * A guest of [owner] standing on [tile] in their house
     */
    private fun guest(owner: Player, tile: Tile): Player {
        val guest = createPlayer(exit, "guest")
        guest.joinInstance(owner.instance()!!)
        guest["house_owner"] = owner.accountName
        guest.tele(tile)
        return guest
    }

    @Test
    fun `Climb in and out of a boxing ring`() {
        val player = createOwner()
        player.addHouseRoom("combat_room", room)
        player.addHouseFurniture(room, "combat_ring", "boxing_ring")
        player.equipment.set(EquipSlot.Weapon.index, "boxing_gloves_red")
        player.enterPortal(1)
        val rope = player.objects(room, "boxing_ring").first { it.shape == ObjectShape.WALL_STRAIGHT }
        player.tele(rope.tile)

        player.objectOption(rope, "Climb-over")
        tick(3)

        assertFalse(player.tile == rope.tile)
        assertEquals("boxing", player.get<String>("house_ring"))
        assertTrue(player["in_pvp", false])

        player.objectOption(rope, "Climb-over")
        tick(3)

        assertEquals(rope.tile, player.tile)
        assertNull(player.get<String>("house_ring"))
        assertFalse(player["in_pvp", false])
    }

    @Test
    fun `Can't equip anything but boxing gloves in a boxing ring`() {
        val player = createOwner()
        player.addHouseRoom("combat_room", room)
        player.addHouseFurniture(room, "combat_ring", "boxing_ring")
        player.equipment.set(EquipSlot.Weapon.index, "boxing_gloves_red")
        player.enterPortal(1)
        val rope = player.objects(room, "boxing_ring").first { it.shape == ObjectShape.WALL_STRAIGHT }
        player.tele(rope.tile)
        player.objectOption(rope, "Climb-over")
        tick(3)
        player.inventory.add("bronze_full_helm")

        player.itemOption("Wear", "bronze_full_helm")
        tick()

        assertEquals(1, player.inventory.count("bronze_full_helm"))
        assertTrue(player.containsMessage("other than the boxing gloves"))
    }

    @Test
    fun `Stored pets roam the menagerie until the house is left`() {
        val player = createOwner()
        player.addHouseRoom("menagerie", room)
        player.inventories.inventory("pet_house").add("pet_cat")
        player.enterPortal(1)
        val pets = { NPCs.filter { it.id.startsWith("menagerie_pet_") } }
        tick(5)

        assertEquals(1, pets().size)

        player.leaveHouse()
        tick()

        assertEquals(0, pets().size)
        assertEquals(1, player.inventories.inventory("pet_house").count("pet_cat"))
    }

    @Test
    fun `Climb into a ring over a corner`() {
        val player = createOwner()
        player.addHouseRoom("combat_room", room)
        player.addHouseFurniture(room, "combat_ring", "combat_ring")
        player.enterPortal(1)
        val corner = player.objects(room, "combat_ring").first { it.shape != ObjectShape.WALL_STRAIGHT }
        player.tele(corner.tile)

        player.objectOption(corner, "Climb-over")
        tick(3)

        assertEquals(1, player.tile.distanceTo(corner.tile))
        assertEquals("combat", player.get<String>("house_ring"))
    }

    @Test
    fun `Can't box without boxing gloves`() {
        val player = createOwner()
        player.addHouseRoom("combat_room", room)
        player.addHouseFurniture(room, "combat_ring", "boxing_ring")
        player.enterPortal(1)
        val rope = player.objects(room, "boxing_ring").first { it.shape == ObjectShape.WALL_STRAIGHT }
        player.tele(rope.tile)

        player.objectOption(rope, "Climb-over")
        tick(3)

        assertEquals(rope.tile, player.tile)
        assertTrue(player.containsMessage("boxing gloves"))
    }

    @Test
    fun `Throne room trapdoor floor drops guests into the oubliette`() {
        val owner = createOwner()
        owner.addHouseRoom("throne_room", room)
        owner.addHouseFurniture(room, "throne_room_lever_space", "oak_lever")
        owner.addHouseFurniture(room, "throne_room_floor_space", "trapdoor_4")
        owner.addHouseRoom("oubliette", dungeon)
        owner.enterPortal(1)
        val floor = owner.objects(room, "floor_decoration_basic_wood").first()
        val guest = guest(owner, floor.tile)
        val lever = owner.objects(room, "oak_lever").first()

        owner.tele(lever.tile.addY(-1))
        owner.objectOption(lever, "Pull")
        tick(3)

        assertEquals(floor.tile.level - 1, guest.tile.level)
    }

    @Test
    fun `Steel cage traps guests on the throne room floor`() {
        val owner = createOwner()
        owner.addHouseRoom("throne_room", room)
        owner.addHouseFurniture(room, "throne_room_lever_space", "oak_lever")
        owner.addHouseFurniture(room, "throne_room_floor_space", "steel_cage_2")
        owner.enterPortal(1)
        val floor = owner.objects(room, "floor_decoration_basic_wood").first()
        val guest = guest(owner, floor.tile)
        val lever = owner.objects(room, "oak_lever").first()

        owner.tele(lever.tile.addY(-1))
        owner.objectOption(lever, "Pull")
        tick(3)

        assertTrue(guest.containsMessage("trapped"))
    }

    @Test
    fun `Building an oubliette below a trapdoor adds a ladder`() {
        val owner = createOwner()
        owner.addHouseRoom("throne_room", room)
        owner.addHouseFurniture(room, "throne_room_trapdoor_space", "trapdoor")
        owner.inventory.add("coins", 150000)
        owner.enterPortal(2)
        val trapdoor = owner.objects(room, "oak_trapdoor").first()

        owner.tele(trapdoor.tile.addY(-1))
        owner.objectOption(trapdoor, "Open")
        tickIf { owner.dialogue == null }
        owner.skipDialogues()
        owner.dialogueOption("line1")
        tick(3)

        assertTrue(owner.houseFurnitureIds.contains("oak_ladder"))
        assertEquals(DUNGEON_LEVEL, owner.tile.level - owner.instance()!!.tile.level)
    }

    @Test
    fun `Climb the oubliette ladder up to the throne room`() {
        val owner = createOwner()
        owner.addHouseRoom("throne_room", room)
        owner.addHouseFurniture(room, "throne_room_trapdoor_space", "trapdoor")
        owner.addHouseRoom("oubliette", dungeon)
        owner.addHouseFurniture(dungeon, "oubliette_ladder_space", "oak_ladder")
        owner.enterPortal(1)
        val ladder = owner.objects(dungeon, "oak_ladder").first()

        owner.tele(ladder.tile.addY(-1))
        owner.objectOption(ladder, "Climb")
        tick(5)

        assertEquals(GROUND_LEVEL, owner.tile.level - owner.instance()!!.tile.level)
    }

    @Test
    fun `Dungeon doors are locked to guests in challenge mode`() {
        val owner = createOwner()
        owner.addHouseRoom("dungeon_corridor", dungeon)
        owner.addHouseFurniture(dungeon, "dungeon_door", "oak_door")
        owner.enterPortal(1)
        owner["house_challenge_mode"] = true
        val door = owner.objects(dungeon, "door_309_closed").first()
        val guest = guest(owner, door.tile)

        guest.objectOption(door, "Open")
        tick(3)

        assertTrue(guest.containsMessage("The door is locked."))
    }

    @Test
    fun `Opening dungeon doors doesn't leave their spaces behind`() {
        val owner = createOwner()
        owner.addHouseRoom("dungeon_corridor", dungeon)
        owner.addHouseFurniture(dungeon, "dungeon_door", "oak_door")
        owner.enterPortal(1)
        val door = owner.objects(dungeon, "door_309_closed").first()
        owner.tele(door.tile.addY(1))

        owner.objectOption(door, "Open")
        tick(3)

        assertNull(GameObjects.findOrNull(door.tile, "door_309_closed"))
        val corridor = roomZone(owner.instance()!!.tile.zone, dungeon)
        assertTrue(corridor.toCuboid().none { tile -> GameObjects.at(tile).any { it.id.startsWith("dungeon_door_space") } })
    }

    @Test
    fun `Owner keeps coins in a treasure chest`() {
        val owner = createOwner()
        owner.addHouseRoom("treasure_room", dungeon)
        owner.addHouseFurniture(dungeon, "treasure_room_treasure_space", "oak_chest")
        owner.inventory.add("coins", 1000)
        owner.enterPortal(1)
        val chest = owner.objects(dungeon, "oak_treasure_room_chest").first()

        owner.tele(chest.tile.addY(-1))
        owner.objectOption(chest, "Open")
        tickIf { owner.dialogue == null }
        owner.dialogueOption("line1")
        owner.intEntry(600)
        tick()

        assertEquals(600, owner["house_treasure", 0])
        assertEquals(400, owner.inventory.count("coins"))
    }

    @Test
    fun `Upgrade a tool store`() {
        val owner = createOwner()
        owner.addHouseRoom("workshop", room)
        owner.addHouseFurniture(room, "workshop_tool", "tool_store_1")
        owner.inventory.add("hammer", "saw")
        owner.inventory.add("oak_plank", 2)
        owner.enterPortal(2)
        val store = owner.objects(room, "tool_store_1").first()

        owner.tele(store.tile.addY(-1))
        owner.objectOption(store, "Upgrade")
        tickIf { !owner.hasOpen("furniture_creation") }
        owner.interfaceOption("furniture_creation", "items", "Build", item = Item("tool_store_2"), slot = 0)

        assertEquals(listOf("exit_portal", "tool_store_2"), owner.houseFurnitureIds)
    }
}
