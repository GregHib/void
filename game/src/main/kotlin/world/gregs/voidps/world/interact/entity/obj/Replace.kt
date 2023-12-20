package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile

object Replace {

    /**
     * Replaces two existing map objects with replacements provided.
     * The replacements can be temporary or permanent if [ticks] is -1
     */
    fun objects(
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
}