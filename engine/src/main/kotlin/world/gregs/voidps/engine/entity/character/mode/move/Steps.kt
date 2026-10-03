package world.gregs.voidps.engine.entity.character.mode.move

import org.rsmod.game.pathfinder.Route
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Queue of [Step]s stored as raw ids in an array, as [peek] is read every tick for every moving character
 */
class Steps(
    internal val character: Character,
) : AbstractList<Step>() {
    private val ids = IntArray(if (smartPathing(character)) MAX_STEPS else SINGLE_STEP)
    private var head = 0
    private var count = 0

    var destination: Tile = Tile.EMPTY
        private set
    var previous: Tile = Tile.EMPTY
    var follow: Tile = Tile.EMPTY
    var movedFrom: Tile = Tile.EMPTY
    var last = 0

    override val size: Int
        get() = count

    override fun isEmpty(): Boolean = count == 0

    override fun get(index: Int): Step {
        if (index !in 0 until count) {
            throw IndexOutOfBoundsException("Index: $index, Size: $count")
        }
        return Tile(ids[head + index])
    }

    fun peek(): Step? = if (count == 0) null else Tile(ids[head])

    fun poll(): Step {
        if (count == 0) {
            throw NoSuchElementException("No steps remaining.")
        }
        val id = ids[head++]
        if (--count == 0) {
            head = 0
        }
        return Tile(id)
    }

    private fun add(step: Step) {
        if (head + count == ids.size) {
            return
        }
        ids[head + count++] = step.id
    }

    private fun lastStep(): Step? = if (count == 0) null else Tile(ids[head + count - 1])

    fun queueRoute(route: Route, target: Tile? = null, noCollision: Boolean = false, noRun: Boolean = false) {
        clearSteps()
        for (waypoint in route.waypoints) {
            add(character.tile.copy(waypoint.x, waypoint.z).step(noCollision, noRun))
        }
        destination = (target ?: lastStep() ?: character.tile).step(noCollision, noRun)
    }

    fun queueStep(tile: Tile, noCollision: Boolean = false, noRun: Boolean = false) {
        clearSteps()
        add(tile.step(noCollision, noRun))
        destination = tile.step(noCollision, noRun)
    }

    fun queueSteps(tiles: List<Tile>, noCollision: Boolean = false, noRun: Boolean = false) {
        clearSteps()
        for (tile in tiles) {
            add(tile.step(noCollision, noRun))
        }
        destination = lastStep() ?: character.tile.step(noCollision, noRun)
    }

    /**
     * Updates all steps to have [noCollision] or [noRun]
     * Used for modifying existing paths, for creating new paths e.g.
     * to walk through doors use [queueSteps]
     */
    fun update(noCollision: Boolean = false, noRun: Boolean = false) {
        for (i in head until head + count) {
            ids[i] = Tile(ids[i]).step(noCollision, noRun).id
        }
        destination = destination.step(noCollision, noRun)
    }

    fun clearDestination() {
        destination = Tile.EMPTY
    }

    private fun clearSteps() {
        head = 0
        count = 0
    }

    fun clear() {
        clearSteps()
        clearDestination()
    }

    companion object {
        private const val MAX_STEPS = 25
        private const val SINGLE_STEP = 1

        fun smartPathing(character: Character) = character is Player || ((character as? NPC)?.ownerIndex ?: -1) != -1
    }
}
