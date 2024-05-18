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
import world.gregs.voidps.engine.entity.character.watching
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.type.Tile

class Follow(
    character: Character,
    val target: Character,
    private val strategy: TargetStrategy = FollowTargetStrategy(target)
) : Movement(character, strategy) {

    private var smart = character is Player

    override fun tick() {
        if (!character.watching(target)) {
            character.watch(target)
        }
        if (target.tile.level != character.tile.level) {
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
            character.steps.clearDestination()
        }
        super.tick()
    }

    override fun recalculate(): Boolean {
        if (character.steps.isEmpty()) {
            smart = false
        }
        if (!equals(strategy.tile, character.steps.destination)) {
            character.steps.queueStep(strategy.tile)
            return true
        }
        return false
    }

    override fun getTarget(): Tile? {
        val target = character.steps.peek()
        if (!smart && target == null) {
            recalculate()
            return character.steps.peek()
        }
        return super.getTarget()
    }

    override fun onCompletion() {
    }

    override fun stop(replacement: Mode) {
        if (replacement !is Follow) {
            character.clearWatch()
        }
    }
}