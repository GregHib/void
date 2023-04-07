package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.character.watching

class Face(
    val character: Character,
    val target: Character,
    val distance: Int = 1
) : Mode {

    override fun tick() {
        if (target["dead", false]) {
            character.mode = EmptyMode
            return
        }

        if (character.tile.distanceTo(target) > distance) {
            character.mode = EmptyMode
            return
        }

        if (!character.watching(target)) {
            character.watch(target)
        }
    }

    override fun stop() {
        super.stop()
        character.clearWatch()
    }
}