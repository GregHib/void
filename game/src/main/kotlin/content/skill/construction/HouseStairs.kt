package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.DUNGEON_LEVEL
import content.skill.construction.House.Companion.UPPER_LEVEL
import content.skill.construction.House.Companion.changeFloor
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseLoading
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.houseRoomRotations
import content.skill.construction.House.Companion.houseStairs
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.House.Companion.roomAbove
import content.skill.construction.House.Companion.roomBelow
import content.skill.construction.House.Companion.roomLevel
import content.skill.construction.House.Companion.roomPosition
import content.skill.construction.House.Companion.stairsDown
import content.skill.construction.RoomCreation.Companion.buildRoom
import content.skill.construction.RoomCreation.Companion.canBuildRoom
import content.skill.construction.RoomCreation.Companion.hasRoomSpace
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Staircases connect rooms with stairs on the floors above and below, the dungeon entrance leads to the dungeon room below it.
 * In building mode the owner is offered to build the room if there isn't one.
 */
class HouseStairs : Script {
    init {
        objectOperate("Enter", "dungeon_entrance") { (target) ->
            val base = houseBase() ?: return@objectOperate
            val owner = houseOwner() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val below = roomBelow(position)
            travel(base, position, below, below in owner.houseRoomPositions, entranceRooms, "This entrance does not lead anywhere.", "below") // TODO proper message
        }

        objectOperate("Climb-up", STAIRCASES) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = true)
        }

        objectOperate("Climb-down", STAIRCASES) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = false)
        }

        objectOperate("Climb", STAIRCASES) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = !target.id.endsWith("_down"))
        }
    }

    /**
     * Climb the stairs in the room at [position] to the stairs leading the other way in the room above or below
     */
    private suspend fun Player.climb(base: Zone, position: Int, up: Boolean) {
        val owner = houseOwner() ?: return
        val level = roomLevel(position)
        if (owner.stairsDown(position) == up || (if (up) level >= UPPER_LEVEL else level <= DUNGEON_LEVEL)) {
            statement(NOWHERE)
            return
        }
        val next = if (up) roomAbove(position) else roomBelow(position)
        val connected = owner.houseStairs(next) != null && owner.stairsDown(next) == up
        // Stairs lead between halls, or down from a hall to the dungeon stairs
        val rooms = if (up || level == UPPER_LEVEL) hallRooms else stairRooms
        travel(base, position, next, connected, rooms, NOWHERE, if (up) "at the top" else "at the bottom")
    }

    /**
     * Moves from the room at [from] to the one at [to] if they're [connected],
     * otherwise offers the owner in building mode to build one of [rooms] [place].
     */
    private suspend fun Player.travel(base: Zone, from: Int, to: Int, connected: Boolean, rooms: List<Pair<String, String>>, nowhere: String, place: String) {
        val destination = tile.addLevel(roomLevel(to) - roomLevel(from))
        if (connected) {
            arrive(destination)
            return
        }
        if (!inOwnHouse() || to in houseRoomPositions) {
            statement(nowhere)
            return
        }
        if (!get("house_build_mode", false)) {
            statement("$nowhere<br>Enter building mode to build a room $place.")
            return
        }
        if (!hasRoomSpace()) {
            return
        }
        statement("$nowhere<br>Do you want to build a room $place?")
        choice {
            for ((room, name) in rooms) {
                option(name) {
                    build(base, room, from, to, destination)
                }
            }
            option("Cancel")
        }
    }

    /**
     * Builds [room] at [to] facing the same way as the room at [from] so the stairs line up, then moves into it
     */
    private suspend fun Player.build(base: Zone, room: String, from: Int, to: Int, destination: Tile) {
        if (houseBase() != base || !inOwnHouse() || to in houseRoomPositions || !canBuildRoom(room, to)) {
            return
        }
        val index = houseRoomPositions.indexOf(from)
        buildRoom(base, room, to, if (index == -1) 0 else houseRoomRotations[index])
        if (to in houseRoomPositions) {
            arrive(destination)
        }
    }

    /**
     * Moves to [tile] showing the loading screen when entering or leaving the dungeon as the map is re-rendered
     */
    private suspend fun Player.arrive(tile: Tile) {
        val hidden = get("hide_upper_levels", false)
        changeFloor(tile)
        if (hidden != get("hide_upper_levels", false)) {
            houseLoading()
        }
    }

    companion object {
        private const val NOWHERE = "These stairs do no lead anywhere."
        private const val STAIRCASES = "oak_staircase,oak_staircase_down,teak_staircase,teak_staircase_down,spiral_staircase,spiral_staircase_down,marble_staircase,marble_staircase_down,marble_spiral,marble_spiral_down"

        // Rooms which can be built at the other end of stairs or below the dungeon entrance
        private val hallRooms = listOf("skill_hall" to "Skill hall", "quest_hall" to "Quest hall")
        private val stairRooms = listOf("dungeon_stairs" to "Dungeon stairs room")
        private val entranceRooms = listOf("dungeon_stairs" to "Dungeon stairs room", "dungeon_corridor" to "Dungeon corridor", "dungeon_junction" to "Dungeon junction")
    }
}
