package content.skill.construction

import content.quest.clearInstance
import content.quest.exitInstance
import content.quest.instance
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Houses are a [HOUSE_SIZE] grid of zones, a [ROOM_GRID] area of rooms around the [START_ROOM] with space for a ring of grass.
 * Rooms are on [GROUND_LEVEL] and [UPPER_LEVEL] with the dungeon on [DUNGEON_LEVEL]
 */
class House : Script {
    init {
        moved {
            if (!contains("house_owner")) {
                return@moved
            }
            val instance = instance()
            if (instance != null && Instances.owner(tile) == instance) {
                return@moved
            }
            leaveHouse(teleport = false)
        }

        playerDespawn {
            if (inOwnHouse()) {
                expelGuests()
            }
        }
    }

    companion object {
        const val ROOM_GRID = 7
        const val HOUSE_LEVELS = 4
        const val HOUSE_SIZE = ROOM_GRID + 2
        const val HOUSE_CENTRE = ROOM_GRID / 2
        const val DUNGEON_LEVEL = 0
        const val GROUND_LEVEL = 1
        const val UPPER_LEVEL = 2
        const val START_ROOM = HOUSE_CENTRE + HOUSE_CENTRE * ROOM_GRID + GROUND_LEVEL * ROOM_GRID * ROOM_GRID

        val Player.houseRoomIds: List<String>
            get() = get("house_room_ids") ?: emptyList()

        val Player.houseRoomPositions: List<Int>
            get() = get("house_room_positions") ?: emptyList()

        val Player.houseRoomRotations: List<Int>
            get() = get("house_room_rotations") ?: emptyList()

        val Player.houseFurnitureRooms: List<Int>
            get() = get("house_furniture_rooms") ?: emptyList()

        val Player.houseFurnitureHotspots: List<String>
            get() = get("house_furniture_hotspots") ?: emptyList()

        val Player.houseFurnitureIds: List<String>
            get() = get("house_furniture_ids") ?: emptyList()

        fun Player.addHouseRoom(id: String, position: Int, rotation: Int = 0) {
            set("house_room_ids", houseRoomIds + id)
            set("house_room_positions", houseRoomPositions + position)
            set("house_room_rotations", houseRoomRotations + rotation)
        }

        /**
         * Replaces any existing rooms with a new house containing just the starting garden and its exit portal
         */
        fun Player.newHouse() {
            set("house_room_ids", listOf("garden"))
            set("house_room_positions", listOf(START_ROOM))
            set("house_room_rotations", listOf(0))
            set("house_furniture_rooms", listOf(START_ROOM))
            set("house_furniture_hotspots", listOf("garden_centrepiece_space"))
            set("house_furniture_ids", listOf("exit_portal"))
        }

        /**
         * Whether the players saved rooms are present and consistent
         */
        fun Player.hasHouse(): Boolean {
            val size = houseRoomIds.size
            return size > 0 && houseRoomPositions.size == size && houseRoomRotations.size == size
        }

        fun Player.removeHouseRoom(position: Int) {
            val index = houseRoomPositions.indexOf(position)
            if (index == -1) {
                return
            }
            set("house_room_ids", houseRoomIds.filterIndexed { i, _ -> i != index })
            set("house_room_positions", houseRoomPositions.filterIndexed { i, _ -> i != index })
            set("house_room_rotations", houseRoomRotations.filterIndexed { i, _ -> i != index })
            removeHouseFurniture(position)
        }

        /**
         * The furniture built on [hotspot] in the room at [position]
         */
        fun Player.houseFurniture(position: Int, hotspot: String): String? {
            val rooms = houseFurnitureRooms
            val hotspots = houseFurnitureHotspots
            val index = rooms.indices.firstOrNull { rooms[it] == position && hotspots[it] == hotspot } ?: return null
            return houseFurnitureIds[index]
        }

        /**
         * Number of exit portals in the house, or only those in the room at [position]
         */
        fun Player.exitPortals(position: Int? = null): Int {
            val rooms = houseFurnitureRooms
            val ids = houseFurnitureIds
            return ids.indices.count { ids[it] == "exit_portal" && (position == null || rooms[it] == position) }
        }

        fun Player.addHouseFurniture(position: Int, hotspot: String, id: String) {
            set("house_furniture_rooms", houseFurnitureRooms + position)
            set("house_furniture_hotspots", houseFurnitureHotspots + hotspot)
            set("house_furniture_ids", houseFurnitureIds + id)
        }

        /**
         * Removes the furniture built in the room at [position], or only that built on [hotspot], resetting their hotspots variants
         */
        fun Player.removeHouseFurniture(position: Int, hotspot: String? = null) {
            val rooms = houseFurnitureRooms
            val hotspots = houseFurnitureHotspots
            val ids = houseFurnitureIds
            val keep = rooms.indices.filter { rooms[it] != position || (hotspot != null && hotspots[it] != hotspot) }
            for (index in rooms.indices - keep.toSet()) {
                val variant = Tables.stringOrNull("house_hotspots.${hotspots[index]}.variant") ?: continue
                clear(variant)
            }
            set("house_furniture_rooms", keep.map { rooms[it] })
            set("house_furniture_hotspots", keep.map { hotspots[it] })
            set("house_furniture_ids", keep.map { ids[it] })
        }

        /**
         * The hotspot [obj] belongs to, hotspots in a group share the group name
         */
        fun hotspot(obj: GameObject): String? {
            val row = Rows.getOrNull("house_hotspots.${obj.id}") ?: return null
            return row.stringOrNull("group") ?: obj.id
        }

        /**
         * Replaces each piece of [hotspot] in [zone] with the matching piece of [furniture], pieces without one are removed.
         * Furniture objects are listed by piece unless [furniture] lists which pieces they're placed on,
         * only placed on tiles with one of its anchor pieces when it has any, and turned by its rotations.
         * Furniture or hotspots with a variant use the object picked by that player variable instead.
         */
        fun Player.placeFurniture(zone: Zone, hotspot: String, furniture: String) {
            val objects = Tables.objList("house_furniture.$furniture.objects")
            val pieces = Tables.intListOrNull("house_furniture.$furniture.pieces")
            val anchors = Tables.intListOrNull("house_furniture.$furniture.anchors")
            val rotations = Tables.intListOrNull("house_furniture.$furniture.rotations")
            val variant = Tables.stringOrNull("house_furniture.$furniture.variant") ?: Tables.stringOrNull("house_hotspots.$hotspot.variant")
            for (tile in zone.toCuboid()) {
                val spaces = GameObjects.at(tile).filter { hotspot(it) == hotspot }
                val anchored = anchors == null || spaces.any { piece(it) in anchors }
                for (obj in spaces) {
                    val piece = piece(obj)
                    val index = if (variant != null) get(variant, 0) else pieces?.indexOf(piece) ?: piece
                    val id = if (anchored) objects.getOrNull(index) else null
                    if (id == null) {
                        obj.remove()
                    } else {
                        obj.replace(id, rotation = (obj.rotation + (rotations?.get(index) ?: 0)) and 0x3)
                    }
                }
            }
        }

        private fun piece(obj: GameObject) = Tables.int("house_hotspots.${obj.id}.piece")

        /**
         * The staircase built in the room at [position]
         */
        fun Player.houseStairs(position: Int): String? = stairsIndex(position)?.let { houseFurnitureIds[it] }

        private fun Player.stairsIndex(position: Int): Int? {
            val staircases = Tables.itemList("house_hotspots.skill_hall_stair_space.furniture")
            val rooms = houseFurnitureRooms
            val ids = houseFurnitureIds
            return rooms.indices.firstOrNull { rooms[it] == position && ids[it] in staircases }
        }

        /**
         * Whether the staircase built in the room at [position] leads down
         */
        fun Player.stairsDown(position: Int): Boolean {
            val index = stairsIndex(position) ?: return false
            return houseFurnitureHotspots[index] == Tables.objOrNull("house_rooms.${houseRoom(position)}.stairs_down")
        }

        /**
         * Adds the other end of the stairs leading down from the room above, or up from the room below,
         * to the room at [position] if it has space for them. Returns whether stairs were added.
         */
        fun Player.connectStairs(position: Int): Boolean {
            val room = houseRoom(position) ?: return false
            if (houseStairs(position) != null) {
                return false
            }
            val above = roomAbove(position)
            val below = roomBelow(position)
            val (furniture, hotspot) = when {
                houseStairs(above) != null && stairsDown(above) -> houseStairs(above) to Tables.objOrNull("house_rooms.$room.stairs")
                houseStairs(below) != null && !stairsDown(below) -> houseStairs(below) to Tables.objOrNull("house_rooms.$room.stairs_down")
                else -> return false
            }
            if (furniture == null || hotspot == null) {
                return false
            }
            addHouseFurniture(position, hotspot, furniture)
            return true
        }

        /**
         * The room built at [position]
         */
        fun Player.houseRoom(position: Int): String? {
            val index = houseRoomPositions.indexOf(position)
            return if (index == -1) null else houseRoomIds.getOrNull(index)
        }

        /**
         * Removes the staircase from the room at [position]
         */
        fun Player.removeHouseStairs(position: Int) {
            val index = stairsIndex(position) ?: return
            removeHouseFurniture(position, houseFurnitureHotspots[index])
        }

        /**
         * Places the furniture built in the room at [position] and decorates it
         */
        fun Player.furnishRoom(base: Zone, position: Int, buildMode: Boolean) {
            val zone = roomZone(base, position)
            val furniture = houseFurnitureIds
            val hotspots = houseFurnitureHotspots
            for ((index, room) in houseFurnitureRooms.withIndex()) {
                if (room == position) {
                    placeFurniture(zone, hotspots[index], furniture[index])
                }
            }
            val window = Tables.obj("house_styles.${get("house_style", "basic_wood")}.${if (roomLevel(position) == DUNGEON_LEVEL) "wall" else "window"}")
            decorate(zone, window, buildMode)
        }

        fun roomPosition(x: Int, y: Int, level: Int) = x + y * ROOM_GRID + level * ROOM_GRID * ROOM_GRID

        fun roomX(position: Int) = position % ROOM_GRID

        fun roomY(position: Int) = position / ROOM_GRID % ROOM_GRID

        fun roomLevel(position: Int) = position / (ROOM_GRID * ROOM_GRID)

        fun roomAbove(position: Int) = position + ROOM_GRID * ROOM_GRID

        fun roomBelow(position: Int) = position - ROOM_GRID * ROOM_GRID

        /**
         * The zone the room at [position] occupies in the house starting at [base]
         */
        fun roomZone(base: Zone, position: Int): Zone = base.add(roomX(position) + 1, roomY(position) + 1, roomLevel(position))

        /**
         * The room position of [zone] in the house starting at [base], or null if it's outside the room grid
         */
        fun roomPosition(base: Zone, zone: Zone): Int? {
            val x = zone.x - base.x - 1
            val y = zone.y - base.y - 1
            val level = zone.level - base.level
            if (x !in 0 until ROOM_GRID || y !in 0 until ROOM_GRID || level !in 0 until HOUSE_LEVELS) {
                return null
            }
            return roomPosition(x, y, level)
        }

        fun Player.inOwnHouse() = get<String>("house_owner") == accountName

        /**
         * The owner of the house the player is currently in
         */
        fun Player.houseOwner(): Player? {
            val owner: String = get("house_owner") ?: return null
            return if (owner == accountName) this else Players.findByAccount(owner)
        }

        /**
         * South-west zone of the house the player is currently in
         */
        fun Player.houseBase(): Zone? {
            if (!contains("house_owner")) {
                return null
            }
            return instance()?.tile?.zone
        }

        /**
         * The zone of the template at [path] in the players house style
         */
        fun Player.template(path: String): Zone {
            val offset = Tables.tile("house_styles.${get("house_style", "basic_wood")}.offset")
            return Tables.tile(path).add(offset.x, offset.y, offset.level).zone
        }

        /**
         * The zone of the template for [room] at [position], rooms with stairs have a separate template
         * for stairs leading down used on the upper floor, or when the stairs are built leading down.
         */
        fun Player.roomTemplate(room: String, position: Int): Zone {
            val down = Tables.objOrNull("house_rooms.$room.stairs_down")
            val upper = Tables.tileOrNull("house_rooms.$room.upper") != null &&
                (roomLevel(position) > GROUND_LEVEL || (down != null && houseFurniture(position, down) != null))
            return template("house_rooms.$room.${if (upper) "upper" else "template"}")
        }

        /**
         * Builds the players house in the instance starting at [base]. Ground floor and dungeon rooms are surrounded by grass,
         * and by darkness in the dungeon when there is one, indoor rooms without a room above are roofed and every other space is left empty.
         */
        fun Player.loadHouse(base: Zone, buildMode: Boolean) {
            val dynamicZones = get<DynamicZones>()
            val entries = mutableListOf<Triple<Zone, Zone, Int>>()
            val placed = mutableSetOf<Zone>()
            val ids = houseRoomIds
            val rotations = houseRoomRotations
            val positions = houseRoomPositions
            for ((index, position) in positions.withIndex()) {
                val zone = roomZone(base, position)
                placed.add(zone)
                entries.add(Triple(roomTemplate(ids[index], position), zone, rotations[index]))
            }
            val roof = template("house_spaces.roof.template")
            for ((index, position) in positions.withIndex()) {
                val level = roomLevel(position)
                if (level == DUNGEON_LEVEL || level + 1 >= HOUSE_LEVELS || Tables.bool("house_rooms.${ids[index]}.outdoor")) {
                    continue
                }
                val zone = roomZone(base, roomAbove(position))
                if (placed.add(zone)) {
                    entries.add(Triple(roof, zone, 0))
                }
            }
            // Grass and the dungeon share one area, around the rooms on both floors
            val rooms = positions.filter { roomLevel(it) == GROUND_LEVEL || roomLevel(it) == DUNGEON_LEVEL }
            if (rooms.isNotEmpty()) {
                surround(base, rooms, GROUND_LEVEL, template("house_spaces.land.template"), placed, entries)
            }
            if (positions.any { roomLevel(it) == DUNGEON_LEVEL }) {
                surround(base, rooms, DUNGEON_LEVEL, Tables.tile("house_spaces.dungeon.${if (buildMode) "build" else "template"}").zone, placed, entries)
            }
            for (level in 0 until HOUSE_LEVELS) {
                for (x in 0 until HOUSE_SIZE) {
                    for (y in 0 until HOUSE_SIZE) {
                        val zone = base.add(x, y, level)
                        if (zone !in placed && dynamicZones.dynamicZone(zone) != null) {
                            dynamicZones.clear(zone)
                        }
                    }
                }
            }
            dynamicZones.copy(entries)
            for (position in positions) {
                furnishRoom(base, position, buildMode)
            }
        }

        /**
         * Fills the empty spaces on [level] within one space of all [rooms] with [template]
         */
        private fun surround(base: Zone, rooms: List<Int>, level: Int, template: Zone, placed: MutableSet<Zone>, entries: MutableList<Triple<Zone, Zone, Int>>) {
            for (x in rooms.minOf(::roomX) - 1..rooms.maxOf(::roomX) + 1) {
                for (y in rooms.minOf(::roomY) - 1..rooms.maxOf(::roomY) + 1) {
                    val zone = base.add(x + 1, y + 1, level)
                    if (placed.add(zone)) {
                        entries.add(Triple(template, zone, 0))
                    }
                }
            }
        }

        /**
         * Fills the window spaces in [zone] and removes any hotspots outside of [buildMode]
         */
        private fun decorate(zone: Zone, window: String, buildMode: Boolean) {
            for (tile in zone.toCuboid()) {
                for (obj in GameObjects.at(tile)) {
                    if (obj.id == "house_window_space") {
                        obj.replace(window)
                    } else if (!buildMode && obj.def.containsOption("Build")) {
                        obj.remove()
                    }
                }
            }
        }

        /**
         * Moves to the nearest free tile to [tile] in its room
         */
        fun Player.changeFloor(tile: Tile) {
            val free = tile.zone.toCuboid().filter { !Collisions.check(it, BLOCKED) }.minByOrNull { it.distanceTo(tile) } ?: tile
            tele(free)
        }

        private const val BLOCKED = CollisionFlag.FLOOR or CollisionFlag.FLOOR_DECORATION or CollisionFlag.OBJECT

        suspend fun Player.houseLoading() {
            open("house_loading")
            delay(3)
            close("house_loading")
        }

        fun Player.leaveHouse(teleport: Boolean = true) {
            val owner: String = remove("house_owner") ?: return
            if (hasOpen("house_options")) {
                open("options")
            }
            if (owner == accountName) {
                expelGuests()
            }
            if (teleport) {
                exitInstance()
            } else {
                clearInstance()
            }
        }

        fun Player.expelGuests() {
            for (player in Players) {
                if (player != this && player.get<String>("house_owner") == accountName) {
                    player.leaveHouse()
                }
            }
        }
    }
}
