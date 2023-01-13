package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals

class Follow(
    character: Character,
    val target: Character,
) : Movement(character, FollowTargetStrategy(target)) {

    init {
        character.watch(target)
    }

    private var smart = true

    override fun tick() {
        if (target.tile != character.tile) {
            stop()
            return
        }
        if (!smart) {
            destination = Tile.EMPTY
        }
        super.tick()
    }

    override fun recalculate() {
        super.recalculate()
        smart = false
    }

    override fun getTarget(): Tile? {
        val target = steps.peek()
        if (target == null) {
            smart = false
            recalculate()
        } else if (character.tile.equals(target.x, target.y)) {
            steps.poll()
            recalculate()
        }
        return steps.peek()
    }

    fun stop() {
        character.watch(null)
        character.mode = EmptyMode
    }
}