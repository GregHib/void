package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.clearWatch
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile

class Follow(
    character: Character,
    val target: Character,
    private val strategy: TargetStrategy = FollowTargetStrategy(target)
) : Movement(character, strategy) {

    init {
        character.watch(target)
    }

    private var smart = character is Player

    override fun tick() {
        if (target.tile.plane != character.tile.plane) {
            if (character is NPC) {
                character.tele(strategy.tile, clearMode = false)
            } else {
                character.mode = EmptyMode
            }
            return
        }
        if (character is NPC && character.tile.distanceTo(target) > 15) {
            character.tele(strategy.tile, clearMode = false)
        }
        if (!smart) {
            destination = Tile.EMPTY
        }
        super.tick()
    }

    override fun recalculate(): Boolean {
        if (steps.isEmpty()) {
            smart = false
        }
        if (strategy.tile != destination) {
            queueStep(strategy.tile, forced)
            return true
        }
        return false
    }

    override fun getTarget(): Tile? {
        val target = steps.peek()
        if (!smart && target == null) {
            recalculate()
            return steps.peek()
        }
        return super.getTarget()
    }

    override fun stop() {
        character.clearWatch()
    }
}