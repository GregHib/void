package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

// TODO how to handle pairs, so that when one object is replaced so is the other.

class CustomObjects(
    private val objects: GameObjects,
    private val definitions: ObjectDefinitions
) : Runnable {
    internal data class Timer(
        val objs: Set<GameObject>,
        var ticks: Int,
        val block: () -> Unit
    )

    private val timers: MutableList<Timer> = mutableListOf()

    override fun run() {
        timers.removeIf { timer ->
            if (--timer.ticks == 0) {
                timer.block.invoke()
            }
            timer.ticks <= 0
        }
    }

    fun setTimer(gameObject: GameObject, ticks: Int, block: () -> Unit) {
        if (ticks <= 0) {
            return
        }
        timers.add(Timer(setOf(gameObject), ticks, block))
    }

    fun setTimer(gameObjects: Set<GameObject>, ticks: Int, block: () -> Unit) {
        if (ticks <= 0) {
            return
        }
        timers.add(Timer(gameObjects, ticks, block))
    }

    fun cancelTimer(gameObject: GameObject): Boolean {
        return timers.removeIf { it.objs.contains(gameObject) }
    }


    /**
     * Spawns an object, optionally removing after a set time
     */
    fun spawn(
        id: String,
        tile: Tile,
        type: Int,
        rotation: Int,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ): GameObject {
        val gameObject = GameObject(definitions.get(id).id, tile, type, rotation)
        objects.add(gameObject, collision)
        setTimer(gameObject, ticks) {
            objects.remove(gameObject, collision)
        }
        return gameObject
    }

    /**
     * Removes an object, optionally reverting after a set time
     */
    fun remove(
        original: GameObject,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ) {
        objects.remove(original, collision)
        setTimer(original, ticks) {
            objects.add(original, collision)
        }
    }

    /**
     * Replaces one object with another, optionally reverting after a set time
     */
    fun replace(
        original: GameObject,
        id: String,
        tile: Tile,
        type: Int = 0,
        rotation: Int = 0,
        ticks: Int = -1,
        owner: String? = null,
        collision: Boolean = true
    ): GameObject {
        val replacement = GameObject(definitions.get(id).id, tile, type, rotation)
        objects.remove(original, collision)
        objects.add(replacement, collision)
        setTimer(setOf(original, replacement), ticks) {
            objects.remove(replacement, collision)
            objects.add(original, collision)
        }
        return replacement
    }

    /**
     * Replaces two objects, linking them to the same job so both revert after timeout
     */
    fun replace(
        firstOriginal: GameObject,
        firstReplacement: String,
        firstTile: Tile,
        firstRotation: Int,
        secondOriginal: GameObject,
        secondReplacement: String,
        secondTile: Tile,
        secondRotation: Int,
        ticks: Int,
        firstOwner: String? = null,
        secondOwner: String? = null,
        collision: Boolean = true
    ) {
        val first = GameObject(definitions.get(firstReplacement).id, firstTile, firstOriginal.type, firstRotation)
        val second = GameObject(definitions.get(secondReplacement).id, secondTile, secondOriginal.type, secondRotation)
        objects.remove(firstOriginal, collision)
        objects.remove(secondOriginal, collision)
        objects.add(first, collision)
        objects.add(second, collision)
        setTimer(setOf(firstOriginal, secondOriginal, first, second), ticks) {
            objects.remove(first, collision)
            objects.remove(second, collision)
            objects.add(firstOriginal, collision)
            objects.add(secondOriginal, collision)
        }
    }

    fun clear() {
        timers.clear()
    }
}

/**
 * Removes an existing map [GameObject].
 * The removal can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to removed just for one player.
 * [collision] can also be used to disable collision changes
 */
fun GameObject.remove(ticks: Int = -1, owner: String? = null, collision: Boolean = true) {
    get<CustomObjects>().remove(this, ticks, owner, collision)
}

/**
 * Replaces an existing map objects with [id] [tile] [type] and [rotation] provided.
 * The replacement can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for an object to replaced just for one player.
 * [collision] can also be used to disable collision changes
 */
fun GameObject.replace(id: String, tile: Tile = this.tile, type: Int = this.type, rotation: Int = this.rotation, ticks: Int = -1, owner: String? = null, collision: Boolean = true): GameObject {
    return get<CustomObjects>().replace(this, id, tile, type, rotation, ticks, owner, collision)
}

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replace just for one player.
 */
fun replaceObjectPair(
    firstOriginal: GameObject,
    firstReplacement: String,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: GameObject,
    secondReplacement: String,
    secondTile: Tile,
    secondRotation: Int,
    ticks: Int,
    owner: String? = null,
    collision: Boolean = true
) = get<CustomObjects>().replace(
    firstOriginal,
    firstReplacement,
    firstTile,
    firstRotation,
    secondOriginal,
    secondReplacement,
    secondTile,
    secondRotation,
    ticks,
    owner,
    owner,
    collision
)

/**
 * Spawns a temporary object with [id] [tile] [type] and [rotation] provided.
 * Can be removed after [ticks] or -1 for permanent (until server restarts or removed)
 */
fun spawnObject(
    id: String,
    tile: Tile,
    type: Int,
    rotation: Int,
    ticks: Int = -1,
    owner: String? = null,
    collision: Boolean = true
) = get<CustomObjects>().spawn(
    id,
    tile,
    type,
    rotation,
    ticks,
    owner,
    collision
)