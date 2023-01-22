package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.map.Tile

class Retreat(
    private val npc: NPC,
    private val target: Entity,
    private val spawn: Tile = npc["spawn_tile"],
    private val maxRetreatDistance: Int = npc.def["max_retreat_distance", 25],
    private val maxRadius: Int = npc.def["attack_radius", 8]
) : Movement(npc) {

    override fun tick() {
//        if (!target.exists) {
//            return npc.mode = EmptyMode
//        }
        var direction = getRetreatDirection(npc, target)
        if (npc.cancelRetreat(target)) {
            npc.mode = EmptyMode
            return
        }
        if (direction != null) {
            direction = splitDirectionIfNeeded(direction)
            if (direction == null) {
                npc.mode = EmptyMode
                return
            }
        }
        if (target is Character) {
            npc.watch(target)
        }
        queueStep(npc.tile.add(direction ?: return))
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

    private fun NPC.cancelRetreat(target: Entity): Boolean {
//        if (!exists || dead) {
//          return true
//        }
        return tile.plane != target.tile.plane || target.tile.distanceTo(tile) > maxRetreatDistance
    }

    private fun getRetreatDirection(npc: NPC, target: Entity): Direction? {
        val x = npc.tile.x
        val y = npc.tile.y
        val canGoSouth = (spawn.y - y) < maxRadius
        val canGoNorth = (y - spawn.y) < maxRadius
        val canGoEast = (x - spawn.x) < maxRadius
        val canGoWest = (spawn.x - x) < maxRadius
        return when {
            target.tile.x >= x && target.tile.y >= y -> when {
                !canGoSouth && !canGoWest -> null
                !canGoSouth -> Direction.WEST
                !canGoWest -> Direction.SOUTH
                else -> Direction.SOUTH_WEST
            }
            target.tile.x >= x && target.tile.y < y -> when {
                !canGoNorth && !canGoWest -> null
                !canGoNorth -> Direction.WEST
                !canGoWest -> Direction.NORTH
                else -> Direction.NORTH_WEST
            }
            target.tile.x < x && target.tile.y >= y -> when {
                !canGoSouth && !canGoEast -> null
                !canGoSouth -> Direction.EAST
                !canGoEast -> Direction.SOUTH
                else -> Direction.SOUTH_EAST
            }
            else -> when {
                !canGoNorth && !canGoEast -> null
                !canGoNorth -> Direction.EAST
                !canGoEast -> Direction.NORTH
                else -> Direction.NORTH_EAST
            }
        }
    }
}