package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.getOrNull
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.EntityTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.Tile

/**
 * Keeps [character] within attack range of [target]
 */
class CombatMovement(
    character: Character,
    var target: Character,
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
        if (!attack()) {
            if (character.steps.destination == character.tile) {
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
        }
    }

    private fun stepOut() {
        character.steps.clear()
        for (dir in Direction.cardinal) {
            if (canStep(dir.delta.x, dir.delta.y)) {
                character.steps.queueStep(character.tile.add(dir))
                break
            }
        }
    }

    private fun attack(): Boolean {
        val attackRange = attackRange()
        if (arrived(if (attackRange == 1) -1 else attackRange)) {
            character.steps.clear()
            character.events.emit(CombatReached(target))
            return true
        }
        return false
    }

    private fun retreat(character: NPC): Boolean {
        val wanderRadius = character.def["wander_radius", 8]
        val spawn: Tile = character["respawn_tile"]
        if (!character.tile.within(spawn, wanderRadius)) {
            character.walkTo(spawn)
            return true
        }
        val attackRadius = character.def["attack_radius", 8]
        val target = character.getOrNull<Character>("target")
        if (target != null && !character.tile.within(target.tile, attackRadius)) {
            character.mode = Retreat(character, target)
            return true
        }
        return false
    }

    private fun attackRange(): Int = character["attack_range", if (character is NPC) character.def["attack_range", 1] else 1]

    override fun onCompletion() {
    }

    override fun stop() {
        character.events.emit(CombatStop(target))
    }
}