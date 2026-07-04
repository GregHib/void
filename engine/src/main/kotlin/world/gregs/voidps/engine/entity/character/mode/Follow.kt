package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.type.Tile

class Follow(
    character: Character,
    val target: Character,
    private val strategy: TargetStrategy = FollowTargetStrategy(target),
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
            val followTile = strategy.tile
            val destination = when {
                followTile != Tile.EMPTY && followTile.level == target.tile.level && followTile.distanceTo(target) <= 15 -> followTile
                else -> target.tile
            }
            character.tele(destination, clearMode = false)
            character.watch(target)
            return
        }
        character.walkTrigger()
        // An npc already beside its target just faces it rather than pathing onto the follow tile
        // (checked here as well as in recalculate - a freshly-set Follow calculates a full path on
        // its first tick, which would otherwise bypass the recalculate guard).
        if (character is NPC && target.tile.distanceTo(character) <= 1) {
            character.steps.clearDestination()
            return
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
        // An npc already beside its target just faces it rather than shuffling onto the follow
        // tile; it only moves again once the target walks out of reach.
        if (character is NPC && target.tile.distanceTo(character) <= 1) {
            return false
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
