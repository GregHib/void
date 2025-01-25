package content.entity.obj.door

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import content.entity.obj.door.Door.openDoor
import content.entity.obj.door.Door.tile
import content.entity.obj.door.Gate.isGate
import content.entity.sound.playSound
import java.util.concurrent.TimeUnit

object Door {

    // Delay in ticks before a door closes itself
    private val doorResetDelay = TimeUnit.MINUTES.toTicks(5)

    /**
     * Closes if [door] is open and opens [door] is closed
     */
    fun toggle(player: Player, door: GameObject, def: ObjectDefinition = door.def, ticks: Int = doorResetDelay, collision: Boolean = true) {
        if (door.id.endsWith("_opened")) {
            closeDoor(player, door, def, ticks, collision)
        } else if (door.id.endsWith("_closed")) {
            openDoor(player, door, def, ticks, collision)
        } else {
            player.message("The ${def.name.lowercase()} won't budge.")
        }
    }

    /**
     * Attempt to close [door]
     */
    fun closeDoor(player: Player, door: GameObject, def: ObjectDefinition = door.def, ticks: Int = doorResetDelay, collision: Boolean = true): Boolean {
        val double = DoubleDoor.get(door, def, 1)
        if (resetExisting(door, double)) {
            closeSound(player, def, gate = def.isGate())
            return true
        }

        // Single door
        if (double == null && door.id.endsWith("_opened")) {
            replace(door, def, "_opened", "_closed", 0, 3, ticks, collision)
            closeSound(player, def, gate = false)
            return true
        }

        // Double doors
        if (double != null && door.id.endsWith("_opened") && double.id.endsWith("_opened")) {
            DoubleDoor.close(door, def, double, ticks, collision)
            closeSound(player, def, gate = false)
            return true
        }
        player.message("The ${def.name.lowercase()} won't budge.")
        return false
    }

    private fun closeSound(player: Player, definition: ObjectDefinition, gate: Boolean) {
        val material = if (definition.contains("material")) "${definition["material", "wood"]}_" else ""
        player.playSound(if (gate) "${material}gate_close" else "${material}door_close")
    }

    /**
     * Attempt to open [door]
     */
    fun openDoor(player: Player, door: GameObject, def: ObjectDefinition = door.def, ticks: Int = doorResetDelay, collision: Boolean = true): Boolean {
        val double = DoubleDoor.get(door, def, 0)
        if (resetExisting(door, double)) {
            openSound(player, def, gate = def.isGate())
            return true
        }

        // Single door
        if (double == null && door.id.endsWith("_closed")) {
            replace(door, def, "_closed", "_opened", 1, 1, ticks, collision)
            openSound(player, def, gate = false)
            return true
        }

        // Double doors
        if (double != null && door.id.endsWith("_closed") && double.id.endsWith("_closed")) {
            DoubleDoor.open(door, def, double, ticks, collision)
            openSound(player, def, gate = false)
            return true
        }
        player.message("The ${def.name.lowercase()} won't budge.")
        return false
    }

    private fun openSound(player: Player, definition: ObjectDefinition, gate: Boolean) {
        val material = if (definition.contains("material")) "${definition["material", "wood"]}_" else ""
        player.playSound(if (gate) "${material}gate_open" else "${material}door_open")
    }

    private fun resetExisting(obj: GameObject, double: GameObject?): Boolean {
        val objects: GameObjects = get()
        if (double == null && objects.timers.execute(obj)) {
            return true
        }

        return double != null && (objects.timers.execute(obj) || objects.timers.execute(double))
    }

    /**
     * Replace door [obj] with [next] for [ticks]
     */
    private fun replace(obj: GameObject, def: ObjectDefinition, current: String, next: String, tileRotation: Int, objRotation: Int, ticks: Int, collision: Boolean = true) {
        val hinged = !def.stringId.contains("single")
        obj.replace(
            id = obj.id.replace(current, next),
            tile = if (hinged) tile(obj, tileRotation) else obj.tile,
            rotation = if (hinged) obj.rotation(objRotation) else obj.rotation,
            ticks = ticks,
            collision = collision
        )
    }

    /**
     * Get tile of [gameObject]after being rotated [anticlockwise]
     */
    fun tile(gameObject: GameObject, anticlockwise: Int) = tile(gameObject.tile, gameObject.rotation, anticlockwise)

    /**
     * Get position of tile with [rotation] after being rotated [anticlockwise]
     */
    fun tile(tile: Tile, rotation: Int, anticlockwise: Int): Tile {
        val orientation = Direction.cardinal[rotate(rotation, -anticlockwise)]
        return tile.add(orientation.delta)
    }

    /**
     * Get rotation of object after being rotated [clockwise]
     */
    fun GameObject.rotation(clockwise: Int) = rotate(rotation, clockwise)

    private fun rotate(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3

    fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true) || this["door", false]
}


/**
 * Walks a player through a door which other players can't walk through
 */
private fun Player.enter(door: GameObject, def: ObjectDefinition = door.def, ticks: Int = 3): Tile? {
    if (door.id.endsWith("_opened")) {
        return null
    }
    val direction = door.tile.delta(tile).toDirection()
    val vertical = door.rotation == 0 || door.rotation == 2
    val target = if (vertical && direction.isHorizontal() || !vertical && direction.isVertical()) {
        door.tile
    } else {
        tile(door, 1)
    }
    openDoor(this, door, def, ticks, collision = false)
    return target
}

/**
 * Enter through a doorway
 */
suspend fun Interaction<Player>.enterDoor(door: GameObject, def: ObjectDefinition = door.def, ticks: Int = 3) {
    val tile = player.enter(door, def, ticks) ?: return
    player.walkOverDelay(tile)
}

/**
 * Enter through a door with fixed [delay]
 */
suspend fun Interaction<Player>.enterDoor(door: GameObject, def: ObjectDefinition = door.def, ticks: Int = 3, delay: Int) {
    val tile = player.enter(door, def, ticks) ?: return
    player.walkTo(tile, noCollision = true, forceWalk = true)
    delay(delay)
}