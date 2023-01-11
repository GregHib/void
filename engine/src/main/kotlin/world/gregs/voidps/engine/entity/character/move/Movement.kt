package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import java.util.*

class Movement(
    val character: Character,
    var previousTile: Tile = Tile.EMPTY,
    var delta: Delta = Delta.EMPTY
) {

    val steps = LinkedList<Tile>()
    var partial: Boolean = false
        private set

    fun clearPath() {
        (character as? Player)?.waypoints?.clear()
        steps.clear()
        partial = false
        println("Clear path mov")
        character.moving = false
    }

    fun clear() {
        clearPath()
        reset()
    }

    fun reset() {
    }
}

var Character.running: Boolean
    get() = get("running", false)
    set(value) = set("running", value)

var Character.moving: Boolean
    get() = get("moving", false)
    set(value) = set("moving", value)