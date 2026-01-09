package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.Step
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Keeps [character] within attack range of [target]
 */
class CombatMovement(
    character: Character,
    var target: Character,
    strategy: TargetStrategy = CharacterTargetStrategy(target),
) : Movement(character, strategy) {

    override fun start() {
        if (character is NPC) {
            character.steps.clear()
        }
        character.face(target)
        character.watch(target)
        character.clear("face_entity")
    }

    override fun tick() {
        if (character is Player && character.dialogue != null) {
            return
        }
        if (target.tile.level != character.tile.level) {
            character.mode = EmptyMode
            return
        }
        if (character is NPC) {
            val definition = get<CombatDefinitions>().get(character.id)
            val retreatRange = definition.retreatRange
            val attackRange = definition.attackRange
            val spawn: Tile = character["respawn_tile"]!!
            if (!character.tile.within(spawn, retreatRange + attackRange)) {
                character.mode = EmptyMode
                return
            }
        }
        if (!attack()) {
            var skip: Boolean
            if (character.steps.destination == character.tile || Overlap.isUnder(character.tile, character.size, target.tile, target.size)) {
                stepOut()
                skip = true
            } else {
                val wasEmpty = character.steps.isEmpty()
                character.steps.clearDestination()
                skip = recalculate() && wasEmpty
            }
            super.tick()
            if (skip || attack()) {
                return
            }
            if (character.hasClock("movement_delay") || character.visuals.moved || getTarget() != null || character is NPC) {
                return
            }
            character.cantReach()
            if (character.mode == this) {
                character.mode = EmptyMode
            }
        }
    }

    override fun nextDirection(target: Step?): Direction? {
        if (character !is NPC) {
            return super.nextDirection(target)
        }
        val direction = super.nextDirection(target) ?: return null
        val step = character.tile.add(direction)
        if (outOfRange(character, step)) {
            return null
        }
        return direction
    }

    private fun outOfRange(character: NPC, step: Tile): Boolean {
        val spawn: Tile = character["respawn_tile"] ?: return false
        val retreatRange = get<CombatDefinitions>().get(character.id).retreatRange
        if (step.distanceTo(spawn) > retreatRange) {
            character.mode = EmptyMode
            return true
        }
        return false
    }

    private fun stepOut() {
        clearSteps()
        if (target.mode is CombatMovement || target.mode is Interact) {
            return
        }
        val direction = Direction.cardinal.random(random)
        if (!canStep(direction.delta.x, direction.delta.y)) {
            return
        }
        val step = character.tile.add(direction)
        if (character is NPC && outOfRange(character, step)) {
            return
        }
        character.steps.queueStep(step)
    }

    private fun attack(): Boolean {
        val attackRange = attackRange()
        val melee = attackRange == 1 && character["weapon", Item.EMPTY].def["weapon_type", ""] != "salamander"
        if (arrived(if (melee) -1 else attackRange)) {
            clearSteps()
            combatReached?.invoke(character, target)
            return true
        }
        return false
    }

    private fun attackRange(): Int = character["attack_range", if (character is NPC) character.def["attack_range", get<CombatDefinitions>().get(character.def["combat_def", character.id]).attackRange] else 1]

    override fun onCompletion() {
    }

    override fun stop(replacement: Mode) {
        if (replacement !is CombatMovement || replacement.target != target) {
            if (character is Player) {
                CombatApi.stop(character, target)
            } else if (character is NPC) {
                CombatApi.stop(character, target)
            }
        }
    }

    companion object : AutoCloseable {
        /**
         * Emitted when within attack range of combat target.
         */
        var combatReached: (Character.(Character) -> Unit)? = null

        override fun close() {
            combatReached = null
        }
    }
}
