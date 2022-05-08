package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.obj.replaceObjectPair
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.isDoor
import world.gregs.voidps.engine.utility.isGate
import world.gregs.voidps.engine.utility.isHinged

object Door {

    fun openDoubleDoors(obj: GameObject, double: GameObject, ticks: Int, collision: Boolean = true) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (obj.def.isGate()) {
            replaceGate(obj, double, flip, ticks, collision, "_closed", "_opened", 3, 1, 1)
        } else {
            replaceObjectPair(
                obj,
                obj.id.replace("_closed", "_opened"),
                getTile(obj, 1),
                obj.rotation(if (flip) 1 else 3),
                double,
                double.id.replace("_closed", "_opened"),
                getTile(double, 1),
                double.rotation(if (flip) 3 else 1),
                ticks,
                collision = collision
            )
        }
    }

    fun closeDoubleDoors(obj: GameObject, double: GameObject, ticks: Int) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (obj.def.isGate()) {
            replaceGate(obj, double, flip, ticks, true, "_opened", "_closed", 1, 2, 3)
        } else {
            val mirror = obj.def.mirrored
            replaceObjectPair(
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

    private fun replaceGate(obj: GameObject, double: GameObject, flip: Boolean, ticks: Int, collision: Boolean, current: String, next: String, objRotation: Int, hingeTileRotation: Int, tileRotation: Int) {
        val first = if (flip) double else obj
        val second = if (flip) obj else double
        val tile = getTile(first, hingeTileRotation)
        replaceObjectPair(
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

    fun replaceDoor(obj: GameObject, current: String, next: String, tileRotation: Int, objRotation: Int, ticks: Int) {
        if (obj.def.isHinged()) {
            obj.replace(
                obj.id.replace(current, next),
                getTile(obj, tileRotation),
                obj.type,
                obj.rotation(objRotation),
                ticks
            )
        } else {
            obj.replace(
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

    fun getDoubleDoor(objects: Objects, gameObject: GameObject, clockwise: Int): GameObject? {
        var orientation = Direction.cardinal[gameObject.rotation(clockwise)]
        var door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
        if (door != null && door.def.isDoor()) {
            return door
        }
        orientation = orientation.inverse()
        door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
        if (door != null && door.def.isDoor()) {
            return door
        }
        if (gameObject.def.isGate()) {
            orientation = orientation.rotate(2)
            door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
            if (door != null && door.def.isGate()) {
                return door
            }
            orientation = orientation.inverse()
            door = objects.getType(gameObject.tile.add(orientation.delta), gameObject.type)
            if (door != null && door.def.isGate()) {
                return door
            }
        }
        return null
    }

    private fun GameObject.rotation(clockwise: Int) = rotate(rotation, clockwise)

    private fun rotate(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3
}