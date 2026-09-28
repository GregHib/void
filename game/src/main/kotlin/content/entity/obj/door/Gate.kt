package content.entity.obj.door

import content.entity.obj.Replace
import content.entity.obj.door.Door.rotation
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

object Gate {
    /**
     * Replace an open or closed gate with the alternative
     */
    fun replace(
        player: Player,
        obj: GameObject,
        double: GameObject,
        flip: Boolean,
        ticks: Int,
        collision: Boolean,
        next: (ObjectDefinition) -> String,
        objRotation: Int,
        hingeTileRotation: Int,
        tileRotation: Int,
        onRevert: (() -> Unit)? = null,
    ): Boolean {
        val first = if (flip) double else obj
        val second = if (flip) obj else double
        val tile = Door.tile(first, hingeTileRotation)
        return Replace.objects(
            first,
            next(first.def(player)),
            tile,
            first.rotation(objRotation),
            second,
            next(second.def(player)),
            Door.tile(tile, second.rotation, tileRotation),
            second.rotation(objRotation),
            ticks,
            collision = collision,
            onRevert = onRevert,
        )
    }

    fun ObjectDefinition.isGate() = name.contains("gate", true) && this["gate", true]
}
