package content.skill.agility.shortcut

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.statement
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Grapple(val objects: GameObjects) : Script {

    init {
        objectApproach("Grapple", "lumbridge_broken_raft") { (target) ->
            if (!hasRequirements(ranged = 37, agility = 8, strength = 17)) {
                return@objectApproach
            }
            steps.clear()
            val direction = if (tile.x < 3253) Direction.EAST else Direction.WEST
            // Skip first half if player is stuck on raft somehow
            if (tile.distanceTo(target.tile) > 2) {
                val start = if (direction == Direction.EAST) Tile(3246, 3179) else Tile(3259, 3180)
                if (tile.distanceTo(start) > 1) {
                    message("I can't do that from here, get closer.")
                    return@objectApproach
                }
                face(target)
                delay(2)
                anim("crossbow_accurate")
                sound("grapple_shoot")
                delay(3)
                message("You successfully grapple the raft and tie the rope to a tree.", ChatType.Filter)
                if (direction == Direction.EAST) {
                    lumbridgeTree(grapple = false)
                } else {
                    alKharidTree(grapple = false)
                }
                walkOverDelay(start.add(direction))
                anim("grapple_enter_water")
                areaGfx("big_splash", start.addX(direction.delta.x * 2), delay = 6)
                sound("grapple_splash", 3)
                exactMoveDelay(start.addX(direction.delta.x * 6), 120, direction)
            }
            if (direction == Direction.EAST) {
                walkToDelay(Tile(3252, 3180))
                walkToDelay(Tile(3253, 3180))
                face(Tile(3260, 3180))
            } else {
                walkToDelay(Tile(3252, 3180))
                face(Tile(3244, 3179))
            }
            delay(2)
            anim("crossbow_accurate")
            sound("grapple_shoot")
            delay(3)
            message("You successfully grapple the tree on the opposite bank.", ChatType.Filter)
            if (direction == Direction.EAST) {
                alKharidTree(grapple = true)
            } else {
                lumbridgeTree(grapple = true)
            }
            delay()
            anim("grapple_exit_water")
            areaGfx("big_splash", tile.add(direction), delay = 6)
            sound("grapple_splash", 3)
            val shore = if (direction == Direction.EAST) Tile(3258, 3180) else Tile(3248, 3179)
            exactMoveDelay(shore, 160, direction)
            val end = if (direction == Direction.EAST) Tile(3259, 3180) else Tile(3246, 3179)
            walkOverDelay(end)
        }

        objectOperate("Grapple", "falador_wall_north") {
            walkToDelay(Tile(3006, 3395))
            face(Direction.SOUTH)
            delay()
            if (!hasRequirements(ranged = 19, agility = 11, strength = 37)) {
                return@objectOperate
            }
            anim("grapple_wall_climb")
            gfx("grapple_wall_climb")
            sound("grapple_shoot", delay = 45)
            delay(11)
            clearGfx()
            clearAnim()
            tele(3006, 3394, 1)
        }

        objectOperate("Grapple", "falador_wall_south") {
            walkToDelay(Tile(3005, 3393))
            face(Direction.NORTH)
            delay()
            if (!hasRequirements(ranged = 19, agility = 11, strength = 37)) {
                return@objectOperate
            }
            anim("grapple_wall_climb")
            gfx("grapple_wall_climb")
            sound("grapple_shoot", delay = 45)
            delay(11)
            clearGfx()
            clearAnim()
            tele(3005, 3394, 1)
        }

        objectOperate("Jump", "falador_wall_jump_north") {
            walkToDelay(Tile(3006, 3394, 1))
            if (!has(Skill.Agility, 4)) {
                message("You need an agility level of at least 4 to climb down this wall.")
                return@objectOperate
            }
            anim("jump_down")
            delay(1)
            anim("jump_land")
            tele(3006, 3395, 0)
        }

        objectOperate("Jump", "falador_wall_jump_south") {
            walkToDelay(Tile(3005, 3394, 1))
            if (!has(Skill.Agility, 4)) {
                message("You need an agility level of at least 4 to climb down this wall.")
                return@objectOperate
            }
            anim("jump_down")
            delay(1)
            anim("jump_land")
            tele(3005, 3393, 0)
        }

        objectApproach("Grapple", "catherby_crossbow_tree") { (target) ->
            if (!hasRequirements(ranged = 39, agility = 36, strength = 22)) {
                return@objectApproach
            }
            steps.clear()
            val start = Tile(2841, 3425)
            if (tile !in Areas["water_obselisk_island"]) {
                message("I can't do that from here.")
                return@objectApproach
            }
            walkToDelay(start)
            face(Direction.NORTH)
            delay()
            anim("grapple_aim_fire")
            delay(2)
            anim("crossbow_accurate")
            sound("grapple_shoot")
            delay(3)
            for (y in 3427..3433) {
                objects.add("grapple_rope", Tile(2841, y), rotation = 1, shape = ObjectShape.GROUND_DECOR, ticks = 14)
            }
            objects.add("catherby_rocks_rope", Tile(2841, 3426), rotation = 1, shape = ObjectShape.GROUND_DECOR, ticks = 14)
            target.replace("catherby_crossbow_tree_grapple", ticks = 14)
            delay(4)
            anim("water_obelisk_swim")
            areaGfx("big_splash", Tile(2841, 3428), 6)
            sound("grapple_splash", delay = 6)
            exactMoveDelay(Tile(2841, 3432), delay = 160, direction = Direction.NORTH)
        }

        objectApproach("Grapple", "catherby_rocks") {
            if (!hasRequirements(ranged = 35, agility = 32, strength = 35)) {
                return@objectApproach
            }
            steps.clear()
            if (tile !in Areas["mountain_shortcut_grapple_area"]) {
                message("I can't do that from here.")
                return@objectApproach
            }
            walkToDelay(Tile(2866, 3429))
            face(Direction.EAST)
            delay()
            anim("grapple_aim_fire")
            sound("grapple_shoot", delay = 55)
            delay(2)
            renderEmote("climbing")
            for (x in 2867..2869) {
                objects.add("catherby_grapple_rope", Tile(x, 3429), shape = ObjectShape.GROUND_DECOR, ticks = 14)
            }
            objects.add("catherby_rocks_grapple", Tile(2869, 3429), shape = ObjectShape.GROUND_DECOR, ticks = 14)
            delay()
            walkOverDelay(Tile(2868, 3429))
            clearRenderEmote()
            walkOverDelay(Tile(2869, 3430))
        }

        objectOperate("Grapple", "yanille_grapple_wall") { (target) ->
            val direction = if (tile.y >= target.tile.y) Direction.SOUTH else Direction.NORTH
            walkToDelay(target.tile)
            face(direction)
            delay()
            if (!hasRequirements(ranged = 21, agility = 39, strength = 38)) {
                return@objectOperate
            }
            anim("grapple_wall_climb")
            gfx("grapple_wall_climb")
            sound("grapple_shoot", delay = 45)
            delay(11)
            clearGfx()
            clearAnim()
            var dest = target.tile
            if (direction != Direction.NORTH) {
                dest = target.tile.add(direction)
            }
            tele(dest.copy(level = 1))
        }

        objectOperate("Jump", "yanille_grapple_wall_jump") { (target) ->
            val direction = if (tile.y == target.tile.y) Direction.SOUTH else Direction.NORTH
            walkToDelay(target.tile)
            if (!has(Skill.Agility, 4)) {
                message("You need an agility level of at least 4 to climb down this wall.")
                return@objectOperate
            }
            anim("jump_down")
            delay(1)
            anim("jump_land")
            var dest = target.tile
            if (direction == Direction.SOUTH) {
                dest = target.tile.add(direction)
            }
            tele(dest.copy(level = 0))
        }
    }

    fun Grapple.lumbridgeTree(grapple: Boolean) {
        val tree = objects[Tile(3244, 3179), "strong_yew"]
        tree?.replace("strong_yew_${if (grapple) "grapple" else "rope"}", ticks = 8)
        for (x in 3246..3251) {
            objects.add("grapple_rope", Tile(x, 3179), shape = ObjectShape.GROUND_DECOR, ticks = 8)
        }
    }

    fun Grapple.alKharidTree(grapple: Boolean) {
        val tree = objects[Tile(3260, 3179), "strong_tree"]
        tree?.replace("strong_tree_${if (grapple) "grapple" else "rope"}", ticks = 8)
        for (x in 3254..3259) {
            objects.add("grapple_rope", Tile(x, 3180), shape = ObjectShape.GROUND_DECOR, ticks = 8)
        }
    }

    suspend fun Player.hasRequirements(ranged: Int, agility: Int, strength: Int): Boolean {
        if (!has(Skill.Ranged, ranged) || !has(Skill.Agility, agility) || !has(Skill.Strength, strength)) {
            statement("You need at least $ranged Ranged, $agility Agility and $strength Strength to do that.")
            return false
        }
        return Weapon.hasGrapple(this)
    }
}
