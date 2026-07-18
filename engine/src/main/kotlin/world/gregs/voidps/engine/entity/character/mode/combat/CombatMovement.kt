package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.config.CombatDefinition
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.type.Tile
import kotlin.math.abs

/**
 * Keeps [character] within attack range of [target]
 */
class CombatMovement(
    character: Character,
    var target: Character,
    val strategy: TargetStrategy = TargetStrategy(character, target),
) : Movement(character, strategy) {

    var started = false

    private var unreachableTicks = 0

    override fun start() {
        if (character is NPC) {
            character.steps.clear()
        }
        character.face(target)
        character.watch(target)
        character.clear("face_entity")
    }

    override fun tick() {
        if (target.tile.level != character.tile.level) {
            character.mode = EmptyMode
            return
        }
        if (target["dead", false]) {
            character.mode = EmptyMode
            return
        }
        if (character is NPC && character["owner_index", -1] == -1 && target["owner_index", -1] == -1) {
            // Owned familiars aren't bound by the spawn/aggro leash: they chase whatever their
            // owner directs them at (which can be further than their aggro range) and fall back to
            // following the owner when the fight ends, so they never wander off permanently. An npc
            // fighting a familiar is likewise exempt, so it keeps defending itself instead of
            // de-aggroing and walking back to its spawn while the familiar is still on it.
            val spawn: Tile = character["spawn_tile"] ?: return
            val definition = get<CombatDefinitions>().get(character.transformDef["combat_def", character.id])
            if (!withinAggro(this.target, spawn, definition)) {
                character.mode = EmptyMode
                return
            }
        }
        if (!attack()) {
            var skip: Boolean
            if (Overlap.isUnder(character.tile, character.size, target.tile, target.size)) {
                skip = true
            } else {
                val wasEmpty = character.steps.isEmpty()
                character.steps.clearDestination()
                skip = recalculate() && wasEmpty
            }
            super.tick()
            if (character is NPC && character["owner_index", -1] != -1) {
                // An owned familiar that can't make progress towards a target it isn't yet close
                // to (no path exists, e.g. the target fled somewhere unreachable) gives up after a
                // grace period and falls back to following its owner (EmptyMode -> Follow in
                // NPCTask) rather than freezing. Moving, or already being all but in range (just
                // waiting on a free attack tile in a crowd), resets the grace period.
                if (character.visuals.moved || arrived(attackRange() + 1)) {
                    unreachableTicks = 0
                } else if (++unreachableTicks >= UNREACHABLE_LIMIT) {
                    unreachableTicks = 0
                    character.mode = EmptyMode
                    return
                }
            }
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

    override fun shouldQueueStepOut(): Boolean =
        target.mode !is CombatMovement && target.mode !is Interact

    private fun attack(): Boolean {
        val attackRange = attackRange()
        val melee = attackRange == 1 && character["weapon", Item.EMPTY].def["weapon_type", ""] != "salamander"
        if (arrived(if (melee) -1 else attackRange)) {
            unreachableTicks = 0
            combatReached?.invoke(character, target)
            return true
        }
        return false
    }

    private fun attackRange(): Int {
        val default = if (character is NPC) {
            val def = character.transformDef
            val combatDefinition = get<CombatDefinitions>().get(def["combat_def", character["transform_id", def.stringId]])
            def["attack_range", combatDefinition.attackRange]
        } else 1
        return character["attack_range", default]
    }

    override fun onCompletion() {
    }

    override fun stop(replacement: Mode) {
        if (!started) {
            return
        }
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
         * Ticks an owned familiar may make no progress towards its target before giving up and
         * returning to follow its owner.
         */
        private const val UNREACHABLE_LIMIT = 5

        /**
         * Emitted when within attack range of combat target.
         */
        var combatReached: (Character.(Character) -> Unit)? = null

        override fun close() {
            combatReached = null
        }

        /**
         * The tile a combat leash (aggro/retreat range) is measured from. Owned followers
         * (familiars, identified by the [owner_index] attribute) anchor to their owner's current
         * tile so they don't de-aggro when the owner moves; all other NPCs anchor to [spawn_tile].
         */
        fun NPC.leashAnchor(): Tile? {
            val ownerIndex = this["owner_index", -1]
            if (ownerIndex != -1) {
                Players.indexed(ownerIndex)?.let { return it.tile }
            }
            return this["spawn_tile"]
        }

        fun withinAggro(target: Character, spawn: Tile, definition: CombatDefinition): Boolean {
            val aggroRange = definition.retreatRange + definition.attackRange
            val absX = abs(target.tile.x - spawn.x)
            val absY = abs(target.tile.y - spawn.y)
            if (definition.attackRange == 1 && absX == absY && absX == aggroRange) {
                return false
            }
            return absX <= aggroRange && absY <= aggroRange
        }
    }
}
