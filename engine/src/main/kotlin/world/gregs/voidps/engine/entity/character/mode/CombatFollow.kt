package world.gregs.voidps.engine.entity.character.mode

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile

class CombatFollow(
    character: Character,
    val target: Character,
    private val strategy: TargetStrategy = FollowTargetStrategy(target),
    private val start: () -> Unit = {}
) : Movement(character, strategy) {

    private val validator: LineValidator = get()
    private var swingCount = 0

    override fun start() {
        character.watch(target)
        start.invoke()
    }

    private fun arrived(distance: Int = -1): Boolean {
        if (distance == -1) {
            return strategy.reached(character)
        }
        if (!character.tile.within(strategy.tile, distance)) {
            return false
        }
        return validator.hasLineOfSight(
            srcX = character.tile.x,
            srcY = character.tile.y,
            level = character.tile.plane,
            srcSize = character.size.width,
            destX = strategy.tile.x,
            destY = strategy.tile.y,
            destWidth = strategy.size.width,
            destHeight = strategy.size.height
        )
    }

    override fun tick() {
        if (target.tile.plane != character.tile.plane) {
            character.mode = EmptyMode
            return
        }
        if (arrived(attackRange())) {
            character.events.emit(CombatAttempt(target, swingCount++))
        } else {
            destination = Tile.EMPTY
            recalculate()
        }
        super.tick()
    }

    private fun attackRange(): Int = character["attack_range", if (character is NPC) character.def["attack_range", 1] else 1]

    override fun recalculate(): Boolean {
        if (strategy.tile != destination) {
            queueStep(strategy.tile, forced)
            return true
        }
        return false
    }

    override fun onCompletion() {
    }

    override fun stop() {
        character.events.emit(CombatStop(target))
    }
}