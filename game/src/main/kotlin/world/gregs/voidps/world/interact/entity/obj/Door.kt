package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals

object Door {

    fun openDoubleDoors(obj: GameObject, def: ObjectDefinition, double: GameObject, ticks: Int, collision: Boolean = true) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (def.isGate()) {
            replaceGate(obj, double, flip, ticks, collision, "_closed", "_opened", 3, 1, 1)
        } else {
            replace(
                obj, obj.id.replace("_closed", "_opened"), getTile(obj, 1), obj.rotation(if (flip) 1 else 3),
                double, double.id.replace("_closed", "_opened"), getTile(double, 1), double.rotation(if (flip) 3 else 1),
                ticks,
                collision
            )
        }
    }

    /**
     * Replaces two existing map objects with replacements provided.
     * The replacements can be temporary or permanent if [ticks] is -1
     */
    private fun replace(
        firstOriginal: GameObject,
        firstReplacement: String,
        firstTile: Tile,
        firstRotation: Int,
        secondOriginal: GameObject,
        secondReplacement: String,
        secondTile: Tile,
        secondRotation: Int,
        ticks: Int,
        collision: Boolean = true
    ) {
        val definitions = get<ObjectDefinitions>()
        val firstId = definitions.get(firstReplacement).id
        val secondId = definitions.get(secondReplacement).id
        if (firstId == -1 || secondId == -1) {
            return
        }
        val objects = get<GameObjects>()
        val first = GameObject(firstId, firstTile, firstOriginal.shape, firstRotation)
        val second = GameObject(secondId, secondTile, secondOriginal.shape, secondRotation)
        objects.remove(firstOriginal, collision)
        objects.remove(secondOriginal, collision)
        objects.add(first, collision)
        objects.add(second, collision)
        objects.timers.add(setOf(firstOriginal, secondOriginal, first, second), ticks) {
            objects.remove(first, collision)
            objects.remove(second, collision)
            objects.add(firstOriginal, collision)
            objects.add(secondOriginal, collision)
        }
    }

    fun closeDoubleDoors(obj: GameObject, def: ObjectDefinition, double: GameObject, ticks: Int) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (def.isGate()) {
            replaceGate(obj, double, flip, ticks, true, "_opened", "_closed", 1, 2, 3)
        } else {
            val mirror = def.mirrored
            replace(
                obj,
                obj.id.replace("_opened", "_closed"),
                getTile(obj, if (mirror) 2 else 0),
                obj.rotation(if (flip || mirror) 1 else 3),
                double,
                double.id.replace("_opened", "_closed"),
                getTile(double, if (mirror) 0 else 2),
                double.rotation(if (flip || mirror) 3 else 1),
                ticks
            )
        }
    }

    private fun replaceGate(
        obj: GameObject,
        double: GameObject,
        flip: Boolean,
        ticks: Int,
        collision: Boolean,
        current: String,
        next: String,
        objRotation: Int,
        hingeTileRotation: Int,
        tileRotation: Int
    ) {
        val first = if (flip) double else obj
        val second = if (flip) obj else double
        val tile = getTile(first, hingeTileRotation)
        replace(
            first,
            first.id.replace(current, next),
            tile,
            first.rotation(objRotation),
            second,
            second.id.replace(current, next),
            getTile(tile, second.rotation, tileRotation),
            second.rotation(objRotation),
            ticks,
            collision = collision
        )
    }

    fun replaceDoor(obj: GameObject, def: ObjectDefinition, current: String, next: String, tileRotation: Int, objRotation: Int, ticks: Int) {
        val objects = get<GameObjects>()
        if (def.isHinged()) {
            objects.replace(
                obj,
                obj.id.replace(current, next),
                getTile(obj, tileRotation),
                obj.shape,
                obj.rotation(objRotation),
                ticks
            )
        } else {
            objects.replace(
                obj,
                obj.id.replace(current, next),
                ticks = ticks
            )
        }
    }

    private fun getTile(gameObject: GameObject, anticlockwise: Int) = getTile(gameObject.tile, gameObject.rotation, anticlockwise)

    private fun getTile(tile: Tile, rotation: Int, anticlockwise: Int): Tile {
        val orientation = Direction.cardinal[rotate(rotation, -anticlockwise)]
        return tile.add(orientation.delta)
    }

    fun getDoubleDoor(objects: GameObjects, gameObject: GameObject, def: ObjectDefinition, clockwise: Int): GameObject? {
        var orientation = Direction.cardinal[gameObject.rotation(clockwise)]
        var door = objects.getShape(gameObject.tile.add(orientation.delta), gameObject.shape)
        if (door != null && door.def.isDoor()) {
            return door
        }
        orientation = orientation.inverse()
        door = objects.getShape(gameObject.tile.add(orientation.delta), gameObject.shape)
        if (door != null && door.def.isDoor()) {
            return door
        }
        if (def.isGate()) {
            orientation = orientation.rotate(2)
            door = objects.getShape(gameObject.tile.add(orientation.delta), gameObject.shape)
            if (door != null && door.def.isGate()) {
                return door
            }
            orientation = orientation.inverse()
            door = objects.getShape(gameObject.tile.add(orientation.delta), gameObject.shape)
            if (door != null && door.def.isGate()) {
                return door
            }
        }
        return null
    }

    private fun GameObject.rotation(clockwise: Int) = rotate(rotation, clockwise)

    private fun rotate(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3
}

fun ObjectDefinition.isDoor() = (name.contains("door", true) && !name.contains("trap", true)) || name.contains("gate", true)
fun ObjectDefinition.isGate() = name.contains("gate", true) && id != 10565 && id != 10566 && id != 28690 && id != 28691
fun ObjectDefinition.isHinged() = !stringId.contains("single")