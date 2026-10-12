package content.skill.construction

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.changeFloor
import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.inOwnHouse
import content.skill.construction.HouseConnections.hasEntrance
import content.skill.construction.HouseConnections.houseLadder
import content.skill.construction.HouseConnections.houseStairs
import content.skill.construction.HouseConnections.houseTrapdoor
import content.skill.construction.HouseConnections.removeHouseStairs
import content.skill.construction.HouseConnections.stairsDown
import content.skill.construction.HouseGrid.DUNGEON_LEVEL
import content.skill.construction.HouseGrid.UPPER_LEVEL
import content.skill.construction.HouseGrid.roomAbove
import content.skill.construction.HouseGrid.roomBelow
import content.skill.construction.HouseGrid.roomLevel
import content.skill.construction.HouseGrid.roomPosition
import content.skill.construction.HouseRooms.houseRoom
import content.skill.construction.HouseRooms.houseRoomPositions
import content.skill.construction.HouseRooms.houseRoomRotations
import content.skill.construction.RoomCreation.Companion.buildRoom
import content.skill.construction.RoomCreation.Companion.canBuildRoom
import content.skill.construction.RoomCreation.Companion.hasRoomSpace
import content.skill.construction.RoomCreation.Companion.removeRoom
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.notImplemented
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

/**
 * Staircases connect rooms with stairs on the floors above and below, the dungeon entrance leads to the dungeon room below it.
 * In building mode the owner is offered to build the room if there isn't one, or to remove the room they lead to.
 */
class HouseStairs : Script {
    private val staircases = "oak_staircase,oak_staircase_down,teak_staircase,teak_staircase_down,spiral_staircase,spiral_staircase_down,marble_staircase,marble_staircase_down,marble_spiral,marble_spiral_down"
    private val stairRooms = listOf("skill_hall" to "Skill hall", "quest_hall" to "Quest hall", "dungeon_stairs" to "Dungeon stairs room")

    init {
        objectOperate("Enter", "dungeon_entrance") { (target) ->
            val base = houseBase() ?: return@objectOperate
            val owner = houseOwner() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val below = roomBelow(position)
            val connected = owner.houseRoom(below)?.let { Tables.objOrNull("house_rooms.$it.stairs") } != null
            travel(base, position, below, connected, stairRooms, "These stairs do not lead anywhere.", "at the bottom")
        }

        objectOperate("Open", "oak_trapdoor,teak_trapdoor,mahogany_trapdoor") { (target) ->
            notImplemented()
            return@objectOperate
            val base = houseBase() ?: return@objectOperate
            val owner = houseOwner() ?: return@objectOperate
            if (!inOwnHouse()) {
                message("Only the owner of the house can use the trapdoor.") // TODO proper message
                return@objectOperate
            }
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val below = roomBelow(position)
            anim("climb_down")
            travel(base, position, below, owner.houseLadder(below) != null, listOf("oubliette" to "Oubliette"), "These stairs do not lead anywhere.", "below the throne room")
        }

        objectOperate("Climb", "oak_ladder,teak_ladder,mahogany_ladder") { (target) ->
            val base = houseBase() ?: return@objectOperate
            val owner = houseOwner() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            val above = roomAbove(position)
            anim("climb_up")
            travel(base, position, above, owner.houseTrapdoor(above) != null, listOf("throne_room" to "Throne room"), "These stairs do not lead anywhere.", "below the throne room")// TODO "Do you want to build an oubliette below the throne room?"
        }

        objectOperate("Climb-up", staircases) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = true)
        }

        objectOperate("Climb-down", staircases) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = false)
        }

        objectOperate("Climb", staircases) { (target) ->
            val base = houseBase() ?: return@objectOperate
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            climb(base, position, up = !target.id.endsWith("_down"))
        }

        objectOperate("Remove-room", "$staircases,dungeon_entrance,oak_trapdoor,teak_trapdoor,mahogany_trapdoor,oak_ladder,teak_ladder,mahogany_ladder") { (target) ->
            val base = houseBase()
            if (base == null || !inOwnHouse()) {
                return@objectOperate
            }
            if (!get("house_build_mode", false)) {
                statement("You can only do that in building mode.")
                return@objectOperate
            }
            val position = roomPosition(base, target.tile.zone) ?: return@objectOperate
            removeConnected(position, target.id)
        }
    }

    /**
     * Removes the room above or below [position] that the stairs, entrance, trapdoor or ladder [id] leads to,
     * staircases are also removed from this room as they no longer lead anywhere.
     */
    private suspend fun Player.removeConnected(position: Int, id: String) {
        val staircase = staircases.split(',').contains(id)
        val up = id.endsWith("_ladder") || (staircase && !id.endsWith("_down"))
        val connected = if (up) roomAbove(position) else roomBelow(position)
        val room = houseRoom(connected)
        if (room == null) {
            statement("There is no room ${if (up) "above" else "below"} to remove.")
            return
        }
        if (staircase && roomLevel(position) != DUNGEON_LEVEL && roomLevel(connected) != DUNGEON_LEVEL) {
            if (up) {
                statement("<red>Warning: <black>If you remove this room you may be unable to access the upper floor from the ground.")
            } else {
                statement("<red>Warning: <black>If you remove this room you may be unable to access the room below.")
            }
        }
        removeRoom(connected, room) {
            if (staircase) {
                removeHouseStairs(position)
            }
        }
    }

    /**
     * Climb the stairs in the room at [position] to the stairs leading the other way in the room above or below
     */
    private suspend fun Player.climb(base: Zone, position: Int, up: Boolean) {
        val owner = houseOwner() ?: return
        val level = roomLevel(position)
        if (owner.stairsDown(position) == up || (if (up) level >= UPPER_LEVEL else level <= DUNGEON_LEVEL)) {
            statement("These stairs do not lead anywhere.")
            return
        }
        val next = if (up) roomAbove(position) else roomBelow(position)
        val connected = (owner.houseStairs(next) != null && owner.stairsDown(next) == up) ||
            (up && level == DUNGEON_LEVEL && owner.hasEntrance(next))
        travel(base, position, next, connected, stairRooms, "These stairs do not lead anywhere.", if (up) "at the top" else "at the bottom")
    }

    /**
     * Moves from the room at [from] to the one at [to] if they're [connected],
     * otherwise offers the owner in building mode to build one of [rooms] [place].
     */
    private suspend fun Player.travel(base: Zone, from: Int, to: Int, connected: Boolean, rooms: List<Pair<String, String>>, nowhere: String, place: String) {
        val destination = tile.addLevel(roomLevel(to) - roomLevel(from))
        if (connected) {
            changeFloor(destination)
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
        val underground = roomLevel(to) == DUNGEON_LEVEL
        statement("$nowhere<br>Do you want to build a room $place?")
        choice {
            for ((room, name) in rooms.filter { (room, _) -> Tables.bool("house_rooms.$room.dungeon") == underground }) {
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
    private fun Player.build(base: Zone, room: String, from: Int, to: Int, destination: Tile) {
        if (houseBase() != base || !inOwnHouse() || to in houseRoomPositions || !canBuildRoom(room, to)) {
            return
        }
        val index = houseRoomPositions.indexOf(from)
        buildRoom(base, room, to, if (index == -1) 0 else houseRoomRotations[index])
        if (to in houseRoomPositions) {
            changeFloor(destination)
        }
    }
}
