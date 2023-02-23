package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.incVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Patrol.Companion.MAX_DELAY
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.map.Tile

/**
 * Walks a preset path of [waypoints] pausing at each point for the number of ticks provided.
 * [character] is teleported to the next point when the path is blocked for longer than [MAX_DELAY]
 */
class Patrol(
    character: Character,
    private val waypoints: List<Pair<Tile, Int>>
) : Movement(character) {

    override fun tick() {
        val (waypoint, delay) = waypoint()

        if (character.tile.plane != waypoint.plane) {
            character.mode = EmptyMode
            return
        }

        val blocked = nextDirection(steps.peek()) == null
        if (blocked) {
            character.incVar("patrol_delay")
        } else {
            character.clearVar("patrol_delay")
        }

        // Wait if waypoint has a delay
        if (character.tile == waypoint && character["patrol_delay", 0] < delay) {
            return
        }
        // Teleport if blocked for too long
        if (character.tile != waypoint && character["patrol_delay", 0] > MAX_DELAY) {
            character.tele(waypoint, clearMode = false)
            return
        }

        // Queue the next waypoint
        if (character.tile == waypoint) {
            character.clearVar("patrol_delay")
            character.incVar("patrol_index")
            queueStep(waypoint().first)
        } else if (steps.isEmpty()) {
            queueStep(waypoint)
        }
        super.tick()
    }

    override fun stop() {
        super.stop()
        character.clearVar("patrol_delay")
        character.clearVar("patrol_index")
    }

    private fun waypoint() = waypoints[character["patrol_index", 0].rem(waypoints.size)]

    companion object {
        private const val MAX_DELAY = 30
    }
}