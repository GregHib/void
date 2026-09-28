package content.entity.obj

import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
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
        collision: Boolean = true,
        onRevert: (() -> Unit)? = null,
    ) {
        val firstId = ObjectDefinitions.get(firstReplacement).id
        val secondId = ObjectDefinitions.get(secondReplacement).id
        if (firstId == -1 || secondId == -1) {
            return
        }
        objects(
            listOf(firstOriginal, secondOriginal),
            listOf(
                GameObject(firstId, firstTile, firstOriginal.shape, firstRotation),
                GameObject(secondId, secondTile, secondOriginal.shape, secondRotation),
            ),
            ticks,
            collision,
            onRevert,
        )
    }

    /**
     * Replaces existing map [originals] with [replacements].
     * Any original tile left empty is filled with an invisible placeholder object so nothing
     * else can be put there while replaced (e.g. lighting a fire in an open doorway).
     * The replacements can be temporary or permanent if [ticks] is -1
     */
    fun objects(
        originals: List<GameObject>,
        replacements: List<GameObject>,
        ticks: Int,
        collision: Boolean = true,
        onRevert: (() -> Unit)? = null,
    ) {
        for (original in originals) {
            GameObjects.remove(original, collision)
        }
        for (replacement in replacements) {
            GameObjects.add(replacement, collision)
        }
        val placeholders = originals.associateWith { placeholder(it, replacements, collision) }
        for (placeholder in placeholders.values) {
            // Invisible walls are solid, placeholders are only there to take up space
            GameObjects.add(placeholder ?: continue, collision = false)
        }
        GameObjects.timers.add((originals + replacements + placeholders.values.filterNotNull()).toSet(), ticks) {
            for (replacement in replacements) {
                GameObjects.remove(replacement, collision)
            }
            for ((original, placeholder) in placeholders) {
                if (placeholder != null) {
                    // Removing a placeholder on the originals layer re-adds the original by itself
                    GameObjects.remove(placeholder, collision && sameLayer(placeholder, original))
                }
                if (!GameObjects.contains(original)) {
                    GameObjects.add(original, collision)
                }
            }
            onRevert?.invoke()
        }
    }

    /**
     * Invisible object to fill the space of [original]; a wall where clipped, otherwise a centrepiece.
     */
    private fun placeholder(original: GameObject, replacements: List<GameObject>, collision: Boolean): GameObject? {
        if (replacements.any { it.tile == original.tile }) {
            return null
        }
        val shape = if (collision) ObjectShape.WALL_STRAIGHT else ObjectShape.CENTRE_PIECE_STRAIGHT
        if (GameObjects.getLayer(original.tile, ObjectLayer.layer(shape)) != null) {
            return null
        }
        return GameObject(ObjectDefinitions.get("inviswall").id, original.tile, shape, original.rotation)
    }

    private fun sameLayer(first: GameObject, second: GameObject) = first.tile == second.tile && ObjectLayer.layer(first.shape) == ObjectLayer.layer(second.shape)
}
