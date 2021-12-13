package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.replaceObjectPair
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals
import world.gregs.voidps.engine.utility.isGate

object Door {

    fun openDoubleDoors(obj: GameObject, double: GameObject, ticks: Int, collision: Boolean = true) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (obj.def.isGate()) {
            val first = if (flip) double else obj
            val second = if (flip) obj else double
            val tile = getTile(first, 1)
            replaceObjectPair(
                first,
                first.id.replace("_closed", "_opened"),
                tile,
                getRotation(first, 3),
                second,
                second.id.replace("_closed", "_opened"),
                getTile(tile, second.rotation, 1),
                getRotation(second, 3),
                ticks,
                collision = collision
            )
        } else {// Double doors
            replaceObjectPair(
                obj,
                obj.id.replace("_closed", "_opened"),
                getTile(obj, 1),
                getRotation(obj, if (flip) 1 else 3),
                double,
                double.id.replace("_closed", "_opened"),
                getTile(double, 1),
                getRotation(double, if (flip) 3 else 1),
                ticks,
                collision = collision
            )
        }
    }


    fun getTile(gameObject: GameObject, anticlockwise: Int) = getTile(gameObject.tile, gameObject.rotation, anticlockwise)

    fun getTile(tile: Tile, rotation: Int, anticlockwise: Int): Tile {
        val orientation = Direction.cardinal[getRotation(rotation, -anticlockwise)]
        return tile.add(orientation.delta)
    }

    fun getRotation(gameObject: GameObject, clockwise: Int) = getRotation(gameObject.rotation, clockwise)

    fun getRotation(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3
}