package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.Step
import world.gregs.voidps.engine.entity.character.mode.move.target.FollowTargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Follows [target] on a leash; the npc trails them for as long as both stay within [range] of
 * [spawn] and loses interest the moment either doesn't.
 *
 * Unlike [Follow] - which is written for pets and teleports an npc that falls more than 15 tiles
 * behind or ends up on another level - a leashed npc never teleports. It can only ever be [range]
 * tiles from [spawn], and a target that outruns, teleports or climbs away from it is simply
 * forgotten.
 */
class Leash(
    private val npc: NPC,
    val target: Character,
    private val range: Int,
    private val spawn: Tile = npc["spawn_tile"]!!,
    private val strategy: TargetStrategy = FollowTargetStrategy(target),
) : Movement(npc, strategy) {

    override fun start() {
        npc.watch(target)
    }

    override fun tick() {
        // within also requires matching levels, so climbing away loses interest too
        if (!npc.tile.within(spawn, range) || !target.tile.within(spawn, range)) {
            npc.mode = EmptyMode
            return
        }
        if (!npc.watching(target)) {
            npc.watch(target)
        }
        npc.walkTrigger()
        // An npc already beside its target just faces it rather than shuffling onto the follow
        // tile. A target standing on top of the npc falls through so [stepOut] can move it to a
        // free adjacent tile.
        if (!underTarget() && target.tile.distanceTo(npc) <= 1) {
            npc.steps.clearDestination()
            return
        }
        // Npcs single-step rather than pathfind, so the destination is recalculated every tick
        npc.steps.clearDestination()
        super.tick()
    }

    override fun recalculate(): Boolean {
        if (target.tile.distanceTo(npc) <= 1) {
            return false
        }
        if (equals(strategy.tile, npc.steps.destination)) {
            return false
        }
        npc.steps.queueStep(strategy.tile, noRun = strategy.forceWalk(npc))
        return true
    }

    override fun getTarget(): Step? {
        val step = npc.steps.peek()
        if (step == null) {
            recalculate()
            return npc.steps.peek()
        }
        return super.getTarget()
    }

    /**
     * The target standing on top of the npc forces it out to a free adjacent tile, the same way
     * combat movement steps npcs out from under their opponent.
     */
    override fun stepOut(): Boolean {
        if (npc.def["allowed_under", false] || !underTarget()) {
            return false
        }
        clearSteps()
        for (direction in Direction.cardinal.shuffled(random)) {
            if (canStep(direction.delta.x, direction.delta.y)) {
                npc.steps.queueStep(npc.tile.add(direction), noRun = strategy.forceWalk(npc))
                break
            }
        }
        return true
    }

    private fun underTarget(): Boolean = Overlap.isUnder(npc.tile, npc.size, npc.size, target.tile, target.size, target.size)

    override fun onCompletion() {
    }

    override fun stop(replacement: Mode) {
        npc.clearWatch()
    }
}
