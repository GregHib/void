package world.gregs.voidps.world.interact.entity.obj.door

import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.interact.entity.obj.Replace
import world.gregs.voidps.world.interact.entity.obj.door.Door.rotation

object Gate {
    /**
     * Replace an open or closed gate with the alternative
     */
    fun replace(
        obj: GameObject,
        double: GameObject,
        flip: Boolean,
        ticks: Int,
        collision: Boolean,
        current: String,
        next: String,
        objRotation: Int,
        hingeTileRotation: Int,
        tileRotation: Int,
    ) {
        val first = if (flip) double else obj
        val second = if (flip) obj else double
        val tile = Door.tile(first, hingeTileRotation)
        Replace.objects(
            first,
            first.id.replace(current, next),
            tile,
            first.rotation(objRotation),
            second,
            second.id.replace(current, next),
            Door.tile(tile, second.rotation, tileRotation),
            second.rotation(objRotation),
            ticks,
            collision = collision
        )
    }

    fun ObjectDefinition.isGate() = name.contains("gate", true) && id != 10565 && id != 10566 && id != 28690 && id != 28691 && id != 1557 && id != 1558 && id != 1596 && id != 1597
}