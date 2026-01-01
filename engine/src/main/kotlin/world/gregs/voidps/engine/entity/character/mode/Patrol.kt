package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Patrol.Companion.MAX_DELAY
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.type.Tile

/**
 * Walks a preset path of [waypoints] pausing at each point for the number of ticks provided.
 * [character] is teleported to the next point when the path is blocked for longer than [MAX_DELAY]
 */
class Patrol(
    character: Character,
    private val waypoints: List<Pair<Tile, Int>>,
    private val loop: Boolean = true,
) : Movement(character) {

    override fun tick() {
        val (waypoint, delay) = waypoint()

        if (character.tile.level != waypoint.level) {
            character.mode = EmptyMode
            return
        }

        val blocked = nextDirection(character.steps.peek()) == null
        if (blocked) {
            character.inc("patrol_delay")
        } else {
            character.clear("patrol_delay")
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
            character.clear("patrol_delay")
            character.inc("patrol_index")
            if (!loop && character["patrol_index", 0] >= waypoints.size) {
                character.mode = EmptyMode
            } else {
                character.steps.queueStep(waypoint().first)
            }
        } else if (character.steps.isEmpty()) {
            character.steps.queueStep(waypoint)
        }
        super.tick()
    }

    override fun onCompletion() {
    }

    override fun stop(replacement: Mode) {
        super.stop(replacement)
        character.clear("patrol_delay")
        character.clear("patrol_index")
    }

    private fun waypoint() = waypoints[character["patrol_index", 0].rem(waypoints.size)]

    companion object {
        private const val MAX_DELAY = 30
    }
}
