package content.area.karamja.brimhaven.agility_arena

import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaDamage
import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaExp
import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaFailed
import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaFall
import content.entity.combat.hit.damage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class BrimhavenArenaObstacles : Script {

    val logs = setOf("brimhaven_arena_log_balance", "brimhaven_arena_log_balance_2")
    val ledges = setOf("brimhaven_arena_balancing_ledge", "brimhaven_arena_balancing_ledge_2")
    val planks = setOf("brimhaven_arena_plank", "brimhaven_arena_plank_2", "brimhaven_arena_plank_3")

    init {
        // Ropes hang off the platform edge so are reached from two tiles away
        objectApproach("Swing-on", "brimhaven_arena_rope_swing") { (target) ->
            approachRange(2)
            // Swung across away from the way the rope faces
            val facing = rotationDirection(target.rotation)
            val direction = facing.inverse()
            val start = target.tile.add(facing.delta.x * 2, facing.delta.y * 2)
            walkToDelay(start)
            face(direction)
            if (arenaFailed(1, 0.1)) {
                anim("rope_swing_miss")
                exactMoveDelay(start.add(direction), 60, direction)
                arenaFall(tile, "You missed the rope!")
                return@objectApproach
            }
            anim("rope_swing")
            target.anim("brimhaven_rope_swing")
            delay()
            exactMoveDelay(target.tile.add(direction.delta.x * 3, direction.delta.y * 3), 60, direction)
            arenaExp(20.0)
        }

        objectOperate("Jump-on", "brimhaven_arena_pillar") { (target) ->
            val direction = crossing(target, setOf("brimhaven_arena_pillar"), tile) ?: return@objectOperate
            walkToDelay(target.tile.add(direction.inverse()))
            face(direction)
            anim("stepping_stone_jump")
            exactMoveDelay(target.tile, 30, direction)
            for (i in 0 until 7) {
                delay()
                // Balance can only be lost on the second pillar
                if (i == 1 && arenaFailed(1, 0.15)) {
                    anim("rope_walk_fall_down")
                    delay(2)
                    val side = direction.rotate(-2)
                    arenaFall(tile.add(side.delta.x * 2, side.delta.y * 2), "You lost your balance!")
                    return@objectOperate
                }
                anim("stepping_stone_jump")
                exactMoveDelay(tile.add(direction), 30, direction)
            }
            arenaExp(18.0)
        }

        objectOperate("Climb-over", "brimhaven_arena_low_wall") { (target) ->
            // Two tiles deep along the walkway
            val direction = if (target.width > target.height) {
                if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            } else {
                if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            }
            val start = tile
            face(direction)
            if (arenaFailed(1, 0.05)) {
                anim("low_wall_fall")
                exactMoveDelay(start.add(direction), 30, direction)
                delay(2)
                damage(arenaDamage())
                message("You lost your balance!")
                exactMoveDelay(start, 30, direction)
                return@objectOperate
            }
            anim("low_wall_climb_over")
            exactMoveDelay(start.add(direction.delta.x * 3, direction.delta.y * 3), 90, direction)
            arenaExp(8.0)
        }

        objectOperate("Walk-on", "brimhaven_arena_log_balance*") { (target) ->
            val (direction, end) = approach(target, logs) ?: return@objectOperate
            if (arenaFailed(1, 0.1)) {
                anim("balance_wobble")
                exactMoveDelay(target.tile, 30, direction)
                balanceFall(direction)
                return@objectOperate
            }
            renderEmote("rope_balance")
            walkOverDelay(end)
            clearRenderEmote()
            arenaExp(12.0)
        }

        objectOperate("Walk-on", "brimhaven_arena_balancing_rope") { (target) ->
            val (direction, end) = approach(target, setOf("brimhaven_arena_balancing_rope")) ?: return@objectOperate
            if (arenaFailed(1, 0.1)) {
                anim("balance_wobble")
                exactMoveDelay(target.tile, 30, direction)
                balanceFall(direction)
                return@objectOperate
            }
            renderEmote("rope_balance")
            walkOverDelay(end)
            clearRenderEmote()
            arenaExp(10.0)
        }

        objectOperate("Walk-across", "brimhaven_arena_balancing_ledge*") { (target) ->
            val (direction, end) = approach(target, ledges) ?: return@objectOperate
            val right = target.id == "brimhaven_arena_balancing_ledge_2"
            renderEmote(if (right) "ledge_balance" else "ledge_balance_3")
            if (arenaFailed(1, 0.15)) {
                walkOverDelay(target.tile)
                clearRenderEmote()
                anim(if (right) "ledge_fall_right" else "ledge_fall_left")
                delay(2)
                arenaFall(tile.add(direction.rotate(2)), "You lost your balance!")
                return@objectOperate
            }
            walkOverDelay(end)
            clearRenderEmote()
            arenaExp(16.0)
        }

        objectOperate("Walk-on", "brimhaven_arena_plank*") { (target) ->
            val (direction, end) = approach(target, planks) ?: return@objectOperate
            if (GameObjects.findOrNull(target.tile.add(direction), "brimhaven_arena_plank_broken") != null) {
                walkOverDelay(target.tile.add(direction))
                anim("plank_fall")
                delay()
                arenaFall(tile, "You stepped on a broken piece of plank!")
                return@objectOperate
            }
            walkOverDelay(end)
            arenaExp(6.0)
        }

        objectOperate("Swing-across", "brimhaven_arena_monkey_bars") { (target) ->
            val bars = setOf("brimhaven_arena_monkey_bars")
            val direction = crossing(target, bars, tile) ?: return@objectOperate
            val other = partner(target, bars, direction) ?: return@objectOperate
            val start = centre(target).add(direction.inverse())
            walkToDelay(start)
            face(direction)
            anim("jump_onto_monkey_bars")
            exactMoveDelay(start.add(direction), 30, direction)
            if (arenaFailed(1, 0.15)) {
                anim("monkey_bars_fall")
                delay(4)
                arenaFall(tile, "You missed a hand hold!")
                return@objectOperate
            }
            renderEmote("monkey_bars")
            walkOverDelay(centre(other))
            clearRenderEmote()
            anim("jump_from_monkey_bars")
            exactMoveDelay(tile.add(direction), 30, direction)
            arenaExp(14.0)
        }

        objectOperate("Climb-across", "brimhaven_arena_hand_holds") { (target) ->
            if (!has(Skill.Agility, 20)) {
                message("You need an Agility of at least 20 to get past this obstacle!")
                return@objectOperate
            }
            val holds = setOf("brimhaven_arena_hand_holds")
            val direction = crossing(target, holds, tile) ?: return@objectOperate
            val facing = rotationDirection(target.rotation)
            val wall = facing.rotate(-2)
            val suffix = if (direction == facing) "" else "_reverse"
            // Grab the first hold and swing to the second, shimmy along to the second to last, then jump down below the last
            walkToDelay(target.tile)
            face(wall)
            // Every swing takes two ticks, each starting as the last one catches its hold
            anim("hand_holds_grab$suffix")
            exactMoveDelay(tile.add(direction), 60, wall, startDelay = 15)
            for (i in 0 until 5) {
                // Only the third hand hold can be missed
                if (i == 0 && arenaFailed(20, 0.15)) {
                    anim("hand_holds_fall$suffix")
                    delay(2)
                    arenaFall(tile, "You missed a hand hold!")
                    return@objectOperate
                }
                anim("hand_holds_shimmy$suffix")
                exactMoveDelay(tile.add(direction), 60, wall)
            }
            anim("hand_holds_let_go$suffix")
            exactMoveDelay(tile.add(direction), 60, wall, startDelay = 15)
            delay()
            arenaExp(22.0)
        }
    }

    /**
     * Fall off a log or rope to the side of the travel [direction]
     */
    suspend fun Player.balanceFall(direction: Direction) {
        anim("rope_walk_fall_down")
        delay(2)
        val side = direction.rotate(-2)
        arenaFall(tile.add(side.delta.x * 2, side.delta.y * 2), "You lost your balance!")
    }

    /**
     * Walk up to the end of a crossing, returning the direction across and the tile beyond the far end
     */
    suspend fun Player.approach(target: GameObject, family: Set<String>): Pair<Direction, Tile>? {
        val direction = crossing(target, family, tile) ?: return null
        val other = partner(target, family, direction) ?: return null
        walkToDelay(target.tile.add(direction.inverse()))
        face(direction)
        return direction to other.tile.add(direction)
    }

    /**
     * Direction from [target] to the obstacle at the other end of its crossing. Parallel lanes
     * (planks) also have ends of the same kind nearby, the far end is the one furthest [from] the player.
     */
    fun crossing(target: GameObject, family: Set<String>, from: Tile): Direction? = Direction.cardinal
        .mapNotNull { direction -> partner(target, family, direction)?.let { direction to it } }
        .maxByOrNull { (_, end) -> from.distanceTo(end.tile) }
        ?.first

    fun partner(target: GameObject, family: Set<String>, direction: Direction): GameObject? {
        // Skip the neighbouring tile, it's a parallel lane of the same crossing
        for (i in 2..MAX_LENGTH) {
            val tile = target.tile.add(direction.delta.x * i, direction.delta.y * i)
            return GameObjects.findOrNull(tile) { it.id in family } ?: continue
        }
        return null
    }

    /**
     * Middle tile of a three tile wide monkey bar
     */
    fun centre(bars: GameObject): Tile = if (bars.width > bars.height) bars.tile.addX(1) else bars.tile.addY(1)

    /**
     * The direction an obstacle faces from its map rotation
     */
    private fun rotationDirection(rotation: Int): Direction = when (rotation and 3) {
        0 -> Direction.NORTH
        1 -> Direction.EAST
        2 -> Direction.SOUTH
        else -> Direction.WEST
    }

    companion object {
        private const val MAX_LENGTH = 8
    }
}
