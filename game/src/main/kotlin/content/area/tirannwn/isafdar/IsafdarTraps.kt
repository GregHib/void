package content.area.tirannwn.isafdar

import content.entity.combat.hit.damage
import content.entity.effect.toxin.poison
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class IsafdarTraps : Script {

    init {
        objectOperate("Step-over", "tripwire") { (target) ->
            val (entry, exit, out, direction) = crossing(target)
            this["crossing_trap"] = true
            walkOverDelay(entry)
            anim("tripwire_step_over")
            exactMoveDelay(exit, delay = 30, direction = direction)
            if (Level.success(levels.get(Skill.Agility), 128..250)) { // TODO unknown rate
                message("You successfully step over the tripwire.")
            } else {
                message("You snag the trip wire as you step over it.")
                arrowVolley(target)
                damage(50)
                damage(50)
                poison(this, 20)
            }
            walkOverDelay(out)
            clear("crossing_trap")
        }

        objectOperate("Pass", "isafdar_sticks_trap") { (target) ->
            val (entry, _, out, direction) = crossing(target)
            this["crossing_trap"] = true
            walkToDelay(entry.add(direction.inverse()), forceWalk = true)
            walkOverDelay(entry)
            if (Level.success(levels.get(Skill.Agility), 31..156)) {
                walkOverDelay(out)
                message("You manage to skillfully pass the trap.")
            } else {
                springTrap(target)
            }
            clear("crossing_trap")
        }

        entered("isafdar_tripwire_1", ::tripwire)
        entered("isafdar_tripwire_2", ::tripwire)
        entered("isafdar_tripwire_3", ::tripwire)
        entered("isafdar_tripwire_4", ::tripwire)
        entered("isafdar_tripwire_5", ::tripwire)

        exited("isafdar_tripwire_1", ::exitTrap)
        exited("isafdar_tripwire_2", ::exitTrap)
        exited("isafdar_tripwire_3", ::exitTrap)
        exited("isafdar_tripwire_4", ::exitTrap)
        exited("isafdar_tripwire_5", ::exitTrap)

        entered("isafdar_sticks_trap_1", ::sticks)
        entered("isafdar_sticks_trap_2", ::sticks)
        entered("isafdar_sticks_trap_3", ::sticks)
        entered("isafdar_sticks_trap_4", ::sticks)
        entered("isafdar_sticks_trap_5", ::sticks)
        entered("isafdar_sticks_trap_6", ::sticks)

        exited("isafdar_sticks_trap_1", ::exitTrap)
        exited("isafdar_sticks_trap_2", ::exitTrap)
        exited("isafdar_sticks_trap_3", ::exitTrap)
        exited("isafdar_sticks_trap_4", ::exitTrap)
        exited("isafdar_sticks_trap_5", ::exitTrap)
        exited("isafdar_sticks_trap_6", ::exitTrap)
    }

    // Tagged "border" so walking at the wire crosses it like a border guard gate
    fun tripwire(player: Player, definition: AreaDefinition) {
        player.steps.update(noCollision = true, noRun = true)
        if (player.contains("crossing_trap")) {
            return
        }
        player["delay"] = 2
        val wire = findTrap(definition, "tripwire") ?: return
        player.arrowVolley(wire)
        player.damage(50)
        player.damage(50)
        player.poison(player, 20)
    }

    fun exitTrap(player: Player, definition: AreaDefinition) {
        player.steps.update(noCollision = false, noRun = false)
    }

    // Tagged "border" so walking at the sticks crosses them like a border guard
    // gate; doing so without interacting always springs the trap
    fun sticks(player: Player, definition: AreaDefinition) {
        player.steps.update(noCollision = true, noRun = true)
        if (player.contains("crossing_trap")) {
            return
        }
        val trap = findTrap(definition, "isafdar_sticks_trap") ?: return
        player.strongQueue("sticks_trap") {
            steps.clear()
            springTrap(trap)
        }
    }

    /**
     * The spring flings the player off the trap in a fixed direction
     * determined by the trap's rotation.
     */
    suspend fun Player.springTrap(trap: GameObject) {
        val axisX = trap.width > trap.height
        val direction = if (axisX) {
            if (trap.rotation == 0) Direction.EAST else Direction.WEST
        } else {
            if (trap.rotation == 1) Direction.NORTH else Direction.SOUTH
        }
        val out = if (axisX) {
            Tile(if (direction == Direction.EAST) trap.tile.x + trap.width else trap.tile.x - 1, tile.y, tile.level)
        } else {
            Tile(tile.x, if (direction == Direction.NORTH) trap.tile.y + trap.height else trap.tile.y - 1, tile.level)
        }
        // Flung backwards but the player keeps facing the way they were
        val facing = if (this.direction == Direction.NONE) direction else this.direction
        message("You set off the trap as you pass.")
        trap.anim("woodspring")
        sound("springtrap")
        anim("trap_stumble_back", delay = 10)
        exactMoveDelay(out, delay = 56, direction = facing, startDelay = 10)
        face(facing)
        damage(80)
    }

    fun Player.arrowVolley(wire: GameObject) {
        val axisX = wire.width > wire.height
        for (i in 0 until maxOf(wire.width, wire.height)) {
            val trapTile = if (axisX) wire.tile.addX(i) else wire.tile.addY(i)
            for (side in intArrayOf(-1, 1)) {
                val tile = if (axisX) trapTile.addY(side) else trapTile.addX(side)
                val launcher = GameObjects.findOrNull(tile, "isafdar_arrow_trap") ?: continue
                launcher.anim("arrowtrip")
                launcher.tile.shoot("iron_arrow", this, flightTime = 20, height = 16, endHeight = 45, curve = 45)
            }
        }
        sound("tripwire_with_arrows", delay = 20)
    }

    /**
     * The near trap tile, far trap tile, tile beyond the far side and crossing
     * direction for stepping over [target] from the player's side of it.
     */
    fun Player.crossing(target: GameObject): Crossing {
        val axisX = target.width > target.height
        return if (axisX) {
            val lane = tile.y.coerceIn(target.tile.y, target.tile.y + target.height - 1)
            if (tile.x <= target.tile.x) {
                Crossing(Tile(target.tile.x, lane, tile.level), Tile(target.tile.x + target.width - 1, lane, tile.level), Tile(target.tile.x + target.width, lane, tile.level), Direction.EAST)
            } else {
                Crossing(Tile(target.tile.x + target.width - 1, lane, tile.level), Tile(target.tile.x, lane, tile.level), Tile(target.tile.x - 1, lane, tile.level), Direction.WEST)
            }
        } else {
            val lane = tile.x.coerceIn(target.tile.x, target.tile.x + target.width - 1)
            if (tile.y <= target.tile.y) {
                Crossing(Tile(lane, target.tile.y, tile.level), Tile(lane, target.tile.y + target.height - 1, tile.level), Tile(lane, target.tile.y + target.height, tile.level), Direction.NORTH)
            } else {
                Crossing(Tile(lane, target.tile.y + target.height - 1, tile.level), Tile(lane, target.tile.y, tile.level), Tile(lane, target.tile.y - 1, tile.level), Direction.SOUTH)
            }
        }
    }

    data class Crossing(val entry: Tile, val exit: Tile, val out: Tile, val direction: Direction)

    companion object {
        private fun findTrap(definition: AreaDefinition, id: String): GameObject? {
            for (tile in definition.area) {
                return GameObjects.findOrNull(tile, id) ?: continue
            }
            return null
        }
    }
}
