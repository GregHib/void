package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.distanceTo

/**
 * Faces target when within [distance]
 */
class Face(
    val character: Character,
    val target: Character,
    val distance: Int = 1
) : Mode {

    override fun start() {
        character.watch(target)
    }

    override fun tick() {
        if (target["dead", false]) {
            character.mode = EmptyMode
            return
        }

        if (character.tile.distanceTo(target) > distance) {
            character.mode = EmptyMode
            return
        }
    }

    override fun stop(replacement: Mode) {
        super.stop(replacement)
        character.clearWatch()
    }
}