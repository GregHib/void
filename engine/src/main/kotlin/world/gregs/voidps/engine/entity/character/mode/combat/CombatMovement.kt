package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.Movement
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

/**
 * Keeps [character] within attack range of [target]
 */
class CombatMovement(
    character: Character,
    var target: Character,
    strategy: TargetStrategy = CharacterTargetStrategy(target),
) : Movement(character, strategy) {

    private val wanderRadius = (character as? NPC)?.def?.getOrNull("wander_radius") ?: 5
    private val spawn: Tile? = character["respawn_tile"]

    override fun start() {
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
        if (!attack()) {
            if (character.steps.destination == character.tile || Overlap.isUnder(character.tile, character.size, target.tile, target.size)) {
                stepOut()
            } else {
                character.steps.clearDestination()
                recalculate()
            }
            super.tick()
            if (attack()) {
                return
            }
            if (character is NPC && retreat(character)) {
                return
            }
            if (character.hasClock("movement_delay") || character.visuals.moved || getTarget() != null || character is NPC) {
                return
            }
            character.cantReach()
            if (character.mode == this) {
                character.mode = EmptyMode
            }
        } else if (character is NPC && retreat(character)) {
            return
        }
    }

    private fun stepOut() {
        clearSteps()
        if (target.mode is CombatMovement || target.mode is Interact) {
            return
        }
        character.steps.queueStep(character.tile.add(Direction.cardinal.random()))
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

    private fun retreat(character: NPC): Boolean {
        val spawn = spawn ?: return false
        if (!character.tile.within(spawn, wanderRadius)) {
            character.walkTo(spawn)
            character.stop("in_combat")
            return true
        }
        val attackRadius = character.def["retreat_range", 8]
        val target = character.get<Character>("target")
        if (target != null && !character.tile.within(target.tile, attackRadius)) {
            character.mode = Retreat(character, target)
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
            } else  if (character is NPC) {
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
