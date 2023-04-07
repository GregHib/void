package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.EntityTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile

/**
 * Keeps [character] within attack range of [target]
 */
class CombatMovement(
    character: Character,
    val target: Character,
    strategy: TargetStrategy = EntityTargetStrategy(target),
) : Movement(character, strategy) {

    override fun start() {
        character.watch(target)
    }

    override fun tick() {
        if (character is Player && character.dialogue != null) {
            return
        }
        if (target.tile.plane != character.tile.plane) {
            character.mode = EmptyMode
            return
        }
        val attackRange = attackRange()
        if (arrived(if (attackRange == 1) -1 else attackRange)) {
            clearMovement()
            character.events.emit(CombatReached(target))
        } else {
            destination = Tile.EMPTY
            recalculate()
        }
        super.tick()
    }

    private fun attackRange(): Int = character["attack_range", if (character is NPC) character.def["attack_range", 1] else 1]

    override fun onCompletion() {
    }

    override fun stop() {
        character.events.emit(CombatStop(target))
    }
}