package content.entity.obj

import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
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
        val placeholders = originals.associateWith { placeholder(it, replacements) }
        for (placeholder in placeholders.values) {
            // Invisible walls are solid, placeholders are only there to take up space
            GameObjects.add(placeholder ?: continue, collision = false)
        }
        // Placeholders are left out so they can't cancel (and orphan) another door's timer
        GameObjects.timers.add((originals + replacements).toSet(), ticks) {
            for (replacement in replacements) {
                GameObjects.remove(replacement, collision)
            }
            for ((original, placeholder) in placeholders) {
                if (placeholder != null) {
                    // Never touches collision, removing it re-adds the original without it
                    GameObjects.remove(placeholder, collision = false)
                }
                if (!GameObjects.contains(original)) {
                    GameObjects.add(original, collision)
                } else if (collision && placeholder != null) {
                    GameObjects.remove(original, collision)
                    GameObjects.add(original, collision)
                }
            }
            onRevert?.invoke()
        }
    }

    /**
     * Invisible object filling the exact slot (tile, layer, shape and rotation) [original] vacated.
     * Never uses a different layer as unstored objects there can't be seen and would be overwritten on clients.
     */
    private fun placeholder(original: GameObject, replacements: List<GameObject>): GameObject? {
        val layer = ObjectLayer.layer(original.shape)
        if (replacements.any { it.tile == original.tile && ObjectLayer.layer(it.shape) == layer }) {
            return null
        }
        return GameObject(ObjectDefinitions.get("inviswall").id, original.tile, original.shape, original.rotation)
    }
}
