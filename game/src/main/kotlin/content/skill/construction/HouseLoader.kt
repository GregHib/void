package content.skill.construction

import content.skill.construction.HouseGrid.DUNGEON_LEVEL
import content.skill.construction.HouseGrid.GROUND_LEVEL
import content.skill.construction.HouseGrid.HOUSE_SIZE
import content.skill.construction.HouseGrid.roomAbove
import content.skill.construction.HouseGrid.roomLevel
import content.skill.construction.HouseGrid.roomPosition
import content.skill.construction.HouseGrid.roomSide
import content.skill.construction.HouseGrid.roomX
import content.skill.construction.HouseGrid.roomY
import content.skill.construction.HouseGrid.roomZone
import content.skill.construction.HouseHotspots.hotspot
import content.skill.construction.HouseHotspots.houseFurniture
import content.skill.construction.HouseHotspots.houseFurnitureHotspots
import content.skill.construction.HouseHotspots.houseFurnitureIds
import content.skill.construction.HouseHotspots.houseFurnitureRooms
import content.skill.construction.HouseRooms.houseRoom
import content.skill.construction.HouseRooms.houseRoomIds
import content.skill.construction.HouseRooms.houseRoomPositions
import content.skill.construction.HouseRooms.houseRoomRotations
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Copies a players rooms into their house instance and places the furniture, walls, windows and doors in them
 */
object HouseLoader {
    private val curtains = setOf("torn_curtains", "curtains", "opulent_curtains")
    private const val HABITAT_FLOOR = "Habitat space"
    private const val HABITAT_SPACE_FLOOR = 44843
    private val habitatFloors = mapOf(
        "garden_habitat" to 44498,
        "jungle_habitat" to 44564,
        "desert_habitat" to 44630,
        "polar_habitat" to 44696,
        "volcanic_habitat" to 44762,
    )

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
            if (level == DUNGEON_LEVEL || level + 1 >= 4 || Tables.bool("house_rooms.${ids[index]}.outdoor")) {
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
            surround(base, rooms, DUNGEON_LEVEL, template("house_spaces.dungeon.${if (buildMode) "build" else "template"}"), placed, entries)
        }
        for (level in 0 until 4) {
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
     * The zone of the template at [path] in the players house style
     */
    private fun Player.template(path: String): Zone {
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
        val style = get("house_style", "basic_wood")
        val level = roomLevel(position)
        val wall = Tables.obj("house_styles.$style.wall")
        val window = if (level == DUNGEON_LEVEL) wall else Tables.obj("house_styles.$style.window")
        val doors = Tables.objList("house_styles.$style.doors")
        val positions = houseRoomPositions
        val neighbours = Direction.cardinal.associateWith { side -> roomPosition(base, zone.add(side))?.takeIf { it in positions } }
        val outdoor = Tables.bool("house_rooms.${houseRoom(position)}.outdoor")
        decorate(
            zone,
            buildMode,
            wall,
            window = { side ->
                // Windows only look out onto gardens, roofs and empty spaces
                val neighbour = neighbours[side]
                if (neighbour == null || Tables.bool("house_rooms.${houseRoom(neighbour)}.outdoor")) window else wall
            },
            door = { tile ->
                val side = roomSide(tile)
                val neighbour = neighbours[side]
                val outside = if (neighbour == null) level == GROUND_LEVEL else Tables.bool("house_rooms.${houseRoom(neighbour)}.outdoor")
                when {
                    outside && !outdoor -> doors[if (leftDoor(tile)) 0 else 1]
                    neighbour != null -> if (side.inverse() in doorways(neighbour)) null else wall
                    // Upstairs doorways without a room on the other side would lead out onto the roofs
                    level > GROUND_LEVEL -> wall
                    else -> null
                }
            },
        )
    }

    /**
     * Sides of the room at [position] which have doorways, turned the way the room is rotated
     */
    private fun Player.doorways(position: Int): Set<Direction> {
        val index = houseRoomPositions.indexOf(position)
        if (index == -1) {
            return emptySet()
        }
        val template = roomTemplate(houseRoomIds[index], position)
        val turns = houseRoomRotations[index] * 2
        val sides = mutableSetOf<Direction>()
        for (tile in template.toCuboid()) {
            for (obj in GameObjects.at(tile)) {
                if (obj.id.startsWith("door_hotspot")) {
                    sides.add(roomSide(obj.tile).rotate(turns))
                }
            }
        }
        return sides
    }

    /**
     * Whether the doorway on [tile] is the left half when looking out of the room
     */
    private fun leftDoor(tile: Tile): Boolean {
        val zone = tile.zone.tile
        return when (roomSide(tile)) {
            Direction.WEST -> tile.y - zone.y < 4
            Direction.NORTH -> tile.x - zone.x < 4
            Direction.EAST -> tile.y - zone.y >= 4
            else -> tile.x - zone.x >= 4
        }
    }

    /**
     * Fills the window spaces in [zone] with the [window] for their side and removes any hotspots outside of [buildMode],
     * doorways are filled with the [door] or wall for their tile, or left open when there isn't one.
     * Window spaces filled with [wall] lose any curtains, or curtain hotspots, hung on them.
     */
    private fun decorate(zone: Zone, buildMode: Boolean, wall: String, window: (Direction) -> String, door: (Tile) -> String?) {
        for (tile in zone.toCuboid()) {
            val objects = GameObjects.at(tile)
            val walled = objects.any { it.id == "house_window_space" && window(roomSide(it.tile)) == wall }
            for (obj in objects) {
                if (obj.id == "house_window_space") {
                    obj.replace(window(roomSide(obj.tile)))
                } else if (walled && (obj.id in curtains || obj.id.endsWith("_curtain_space"))) {
                    val space = GameObjects.original(obj)
                    obj.remove()
                    space?.remove()
                } else if (!buildMode && obj.id.startsWith("door_hotspot")) {
                    val wall = door(obj.tile)
                    if (wall == null) obj.remove() else obj.replace(wall)
                } else if (!buildMode && (obj.def.containsOption("Build") || obj.def.name == HABITAT_FLOOR)) {
                    obj.remove(collision = !againstWall(obj))
                }
            }
        }
    }

    /**
     * Whether [obj] is on the edge of its room next to the walls
     */
    private fun againstWall(obj: GameObject): Boolean {
        val zone = obj.tile.zone.tile
        val x = obj.x - zone.x
        val y = obj.y - zone.y
        return x == 0 || y == 0 || x + obj.width >= 8 || y + obj.height >= 8
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
        val floor = habitatFloors[furniture]
        if (floor != null) {
            layHabitatFloor(zone, floor)
        }
    }

    private fun piece(obj: GameObject) = Tables.int("house_hotspots.${obj.id}.piece")

    /**
     * Replaces the floor placeholders of the menagerie in [zone] with the matching tiles of the habitat whose floor objects start at [floor]
     */
    private fun layHabitatFloor(zone: Zone, floor: Int) {
        for (tile in zone.toCuboid()) {
            for (obj in GameObjects.at(tile)) {
                val index = obj.def.id - HABITAT_SPACE_FLOOR
                if (index !in 0 until 64) {
                    continue
                }
                // The garden habitat leaves a couple of tiles bare
                if (ObjectDefinitions.getValue(floor + index).name.endsWith("habitat")) {
                    GameObjects.replace(obj, GameObject(floor + index, tile.x, tile.y, tile.level, obj.shape, obj.rotation))
                } else {
                    obj.remove()
                }
            }
        }
    }
}
