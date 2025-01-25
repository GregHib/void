package content.entity.obj.door

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.equals
import content.entity.obj.Replace
import content.entity.obj.door.Door.isDoor
import content.entity.obj.door.Gate.isGate

object DoubleDoor {
    /**
     * Get the neighbouring door given either one of the door [gameObject]'s
     */
    fun get(gameObject: GameObject, def: ObjectDefinition, clockwise: Int): GameObject? {
        val objects: GameObjects = get()
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

    /**
     * Open a pair of double doors [obj] and [double]
     */
    fun open(obj: GameObject, def: ObjectDefinition, double: GameObject, ticks: Int, collision: Boolean = true) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (def.isGate()) {
            Gate.replace(obj, double, flip, ticks, collision, "_closed", "_opened", 3, 1, 1)
        } else {
            Replace.objects(
                obj, obj.id.replace("_closed", "_opened"), Door.tile(obj, 1), obj.rotation(if (flip) 1 else 3),
                double, double.id.replace("_closed", "_opened"), Door.tile(double, 1), double.rotation(if (flip) 3 else 1),
                ticks,
                collision
            )
        }
    }

    /**
     * Close a pair of double doors [obj] and [double]
     */
    fun close(obj: GameObject, def: ObjectDefinition, double: GameObject, ticks: Int, collision: Boolean = true) {
        val delta = obj.tile.delta(double.tile)
        val dir = Direction.cardinal[obj.rotation]
        val flip = dir.delta.equals(delta.x.coerceIn(-1, 1), delta.y.coerceIn(-1, 1))
        if (def.isGate()) {
            Gate.replace(obj, double, flip, ticks, collision, "_opened", "_closed", 1, 2, 3)
        } else {
            val mirror = def.mirrored
            Replace.objects(
                obj,
                obj.id.replace("_opened", "_closed"),
                Door.tile(obj, if (mirror) 2 else 0),
                obj.rotation(if (flip || mirror) 1 else 3),
                double,
                double.id.replace("_opened", "_closed"),
                Door.tile(double, if (mirror) 0 else 2),
                double.rotation(if (flip || mirror) 3 else 1),
                ticks,
                collision
            )
        }
    }

    private fun GameObject.rotation(clockwise: Int) = rotate(rotation, clockwise)

    private fun rotate(rotation: Int, clockwise: Int) = (rotation + clockwise) and 0x3
}