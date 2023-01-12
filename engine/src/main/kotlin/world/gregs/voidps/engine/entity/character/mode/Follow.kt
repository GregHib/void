package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.equals

class Follow(
    character: Character,
    val target: Character,
    val strategy: TargetStrategy = FollowTargetStrategy(target)
) : Movement(character, strategy) {

    init {
        character.watch(target)
    }

    private var smart = true

    override fun tick() {
        super.tick()
        if (!smart) {
            destination = Tile.EMPTY
//            return super.tick()
        }
//        queueStep(strategy.tile)
    }

    override fun getTarget(): Tile? {
        val target = steps.peek()
        if (target == null) {
            smart = false
            recalculate()
            return null
        }
        if (character.tile.equals(target.x, target.y)) {
            steps.poll()
            recalculate()
            return null
        }
        return target
    }
}