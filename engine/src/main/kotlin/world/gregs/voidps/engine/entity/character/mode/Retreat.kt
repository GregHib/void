package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Retreat(
    private val npc: NPC,
    private val target: Entity,
    private val spawn: Tile = npc["spawn_tile"]!!,
    private val maxRetreatRadius: Int = npc.def["max_retreat_distance", 25],
    private val maxRadius: Int = npc.def["max_retreat_distance", 25],
) : Movement(npc) {

    override fun tick() {
        if (target is Character && target["dead", false]) {
            npc.mode = EmptyMode
            return
        }
        var direction = getRetreatDirection(npc, target)
        if (direction == null) {
            npc.mode = EmptyMode
            return
        }
        direction = splitDirectionIfNeeded(direction)
        if (direction == null) {
            npc.mode = EmptyMode
            return
        }
        if (target is Character) {
            npc.watch(target)
        }
        character.steps.queueStep(npc.tile.add(direction))
        super.tick()
    }

    private fun splitDirectionIfNeeded(direction: Direction): Direction? {
        if (canStep(direction.delta.x, direction.delta.y)) {
            return direction
        }
        if (!direction.isDiagonal()) {
            return null
        }
        val horizontal = direction.horizontal()
        if (canStep(horizontal.delta.x, horizontal.delta.y)) {
            return horizontal
        }
        val vertical = direction.vertical()
        if (canStep(vertical.delta.x, vertical.delta.y)) {
            return vertical
        }
        return null
    }

    private fun getRetreatDirection(npc: NPC, target: Entity): Direction? {
        if (npc.tile.level != target.tile.level || npc.tile.distanceTo(target) > maxRetreatRadius) {
            return null
        }
        val delta = npc.tile.delta(target.tile)
        val direction = when {
            delta.x > 0 && delta.y > 0 -> Direction.NORTH_EAST
            delta.x > 0 -> Direction.SOUTH_EAST
            delta.y > 0 -> Direction.NORTH_WEST
            else -> Direction.SOUTH_WEST
        }
        val add = npc.tile.add(direction)
        val horizontal = add.x in spawn.x - maxRadius..spawn.x + maxRadius
        val vertical = add.y in spawn.y - maxRadius..spawn.y + maxRadius
        return when {
            horizontal && vertical -> direction
            vertical -> if (direction.delta.y == 1) Direction.NORTH else Direction.SOUTH
            horizontal -> if (direction.delta.x == 1) Direction.EAST else Direction.WEST
            else -> null
        }
    }
}
