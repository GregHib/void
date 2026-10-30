package content.area.karamja.brimhaven.agility_arena

import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaDamage
import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaExp
import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaFailed
import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid

/**
 * Traps on the walkways between the arena platforms, sprung by walking onto them.
 */
class BrimhavenArenaTraps : Script {

    init {
        for ((type, count) in listOf("pressure_pad" to 3, "floor_spikes" to 3, "dart_trap" to 3, "spinning_blades" to 3, "saw_blade" to 6)) {
            for (i in 1..count) {
                entered("brimhaven_arena_${type}_$i") { trap(type, it) }
            }
        }

        // The blade can swing out while stood under it, catching the player as they step off
        for (i in 1..6) {
            exited("brimhaven_arena_saw_blade_$i") { leaveSawBlade() }
        }
    }

    fun Player.trap(type: String, definition: AreaDefinition) {
        if (queue.contains("brimhaven_arena_trap")) {
            return
        }
        val from = steps.previous
        var direction = Direction.of((tile.x - from.x).coerceIn(-1, 1), (tile.y - from.y).coerceIn(-1, 1))
        if (direction == Direction.NONE) {
            return
        }
        if (direction.isDiagonal()) {
            val area = definition.area as Cuboid
            direction = if (area.width >= area.height) direction.horizontal() else direction.vertical()
        }
        if (type == "saw_blade" && !bladeSwinging(tile)) {
            return
        }
        steps.clear()
        val trap = tile
        val start = trap.add(direction.inverse())
        strongQueue("brimhaven_arena_trap") {
            when (type) {
                "pressure_pad" -> {
                    delay(3)
                    areaGfx("pressure_pad_rocks", trap)
                    jumpOrKnockBack(trap, start, direction, 20, 0.25, 26.0, "You were hit by some falling rocks!")
                }
                "floor_spikes" -> {
                    delay(3)
                    GameObjects.findOrNull(trap, "brimhaven_arena_floor_spikes")?.anim("floor_spikes")
                    jumpOrKnockBack(trap, start, direction, 20, 0.25, 24.0, "You were hit by some floor spikes!")
                }
                "spinning_blades" -> {
                    delay(3)
                    spinningBlades(trap)?.anim("spinning_blades")
                    jumpOrKnockBack(trap, start, direction, 40, 0.15, 28.0, "You were hit by the spinning blades.")
                }
                "dart_trap" -> darts(trap, start, direction)
                "saw_blade" -> {
                    // Hit while the blade is still out
                    delay(1)
                    anim("saw_blade_hit")
                    exactMoveDelay(start, 30, direction)
                    damage(arenaDamage())
                    message("You were hit by the saw blade!")
                }
            }
        }
    }

    fun Player.leaveSawBlade() {
        val blade = steps.previous
        // Only a single step off the blade, not teleporting, climbing or being moved elsewhere
        if (blade.level != tile.level || blade.distanceTo(tile) != 1 || queue.contains("brimhaven_arena_trap") || !bladeSwinging(blade)) {
            return
        }
        val direction = Direction.of(tile.x - blade.x, tile.y - blade.y)
        steps.clear()
        strongQueue("brimhaven_arena_trap") {
            anim("saw_blade_hit")
            exactMoveDelay(blade, 30, direction)
            damage(arenaDamage())
            message("You were hit by the saw blade!")
        }
    }

    suspend fun Player.jumpOrKnockBack(trap: Tile, start: Tile, direction: Direction, level: Int, chance: Double, experience: Double, message: String) {
        if (arenaFailed(level, chance)) {
            if (levels.get(Skill.Agility) < level) {
                message("You need an Agility of at least $level to get past this trap!")
            }
            anim("trap_knock_back")
            exactMoveDelay(start, 30, direction)
            damage(arenaDamage())
            message(message)
            return
        }
        anim("trap_jump")
        exactMoveDelay(trap.add(direction.delta.x * 2, direction.delta.y * 2), 60, direction)
        arenaExp(experience)
    }

    /**
     * Darts fire from the wall ahead, dodged by ducking under them or knocking the player back,
     * poisoning two of their Agility levels
     */
    suspend fun Player.darts(trap: Tile, start: Tile, direction: Direction) {
        val launcher = trap.add(direction.delta.x * 5, direction.delta.y * 5)
        delay(2)
        if (arenaFailed(40, 0.15)) {
            if (levels.get(Skill.Agility) < 40) {
                message("You need an Agility of at least 40 to get past this trap!")
            }
            launcher.shoot("dart_trap_dart", trap, delay = 20, flightTime = 60, height = 0, endHeight = 10)
            delay(3)
            anim("trap_knock_back")
            exactMoveDelay(start, 30, direction)
            damage(arenaDamage())
            delay(1)
            message("You were hit by some darts, something on them makes you feel dizzy!")
            levels.drain(Skill.Agility, 2)
            return
        }
        moveCamera(launcher.add(direction.delta.x * 4, direction.delta.y * 4), 350, 1, 100)
        turnCamera(trap, 350, 1, 100)
        launcher.shoot("dart_trap_dart", trap.add(direction.delta.x * -4, direction.delta.y * -4), flightTime = 200, height = 0, endHeight = 0)
        delay(2)
        anim("dart_trap_dodge", delay = 20)
        delay(5)
        exactMoveDelay(trap.add(direction.delta.x * 2, direction.delta.y * 2), 60, direction)
        arenaExp(30.0)
        delay(2)
        clearCamera()
    }

    fun spinningBlades(trap: Tile): GameObject? {
        for (x in -1..1) {
            for (y in -1..1) {
                return GameObjects.findOrNull(trap.add(x, y), "brimhaven_arena_spinning_blades") ?: continue
            }
        }
        return null
    }

    /**
     * Whether a swinging saw blade, two tiles long, is out over [tile]
     */
    fun bladeSwinging(tile: Tile): Boolean {
        for (x in -1..0) {
            for (y in -1..0) {
                val blade = GameObjects.findOrNull(tile.add(x, y), "brimhaven_arena_blade") ?: continue
                if (tile.x < blade.tile.x + blade.width && tile.y < blade.tile.y + blade.height) {
                    return true
                }
            }
        }
        return false
    }
}
