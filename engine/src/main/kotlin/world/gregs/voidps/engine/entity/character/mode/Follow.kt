package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile

class Follow(
    character: Character,
    val target: Character,
) : Movement(character, FollowTargetStrategy(target)) {

    init {
        character.watch(target)
    }

    private var smart = true

    override fun tick() {
        if (target.tile.plane != character.tile.plane) {
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
        if (!smart && target == null) {
            recalculate()
            return steps.peek()
        }
        return super.getTarget()
    }

    fun stop() {
        character.watch(null)
        character.mode = EmptyMode
    }
}