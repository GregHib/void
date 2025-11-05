package content.skill.agility.shortcut

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.sound
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import kotlin.math.round

class SteppingStones : Script {

    init {
        objectApproach("Jump-across", "lumbridge_swamp_stepping_stone") { (target) ->
            val direction = if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
            val start = if (direction == Direction.WEST) Tile(3208, 9572) else Tile(3204, 9572)
            val end = if (direction == Direction.WEST) Tile(3204, 9572) else Tile(3208, 9572)
            if (tile != start) {
                walkToDelay(start)
                delay()
            }
            anim("stepping_stone_step", delay = 30)
            message(text = "You leap across with a mighty leap!", ChatType.Filter)
            sound("jump", delay = 35)
            exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            if (Level.success(levels.get(Skill.Agility), 51..252)) {
                anim("stepping_stone_step", delay = 30)
                sound("jump", delay = 35)
                exactMoveDelay(end, startDelay = 58, delay = 70, direction = direction)
                exp(Skill.Agility, 3.0)
            } else {
                message("You slip over on the slimy stone.", ChatType.Filter)
                anim("rope_walk_fall_down")
                areaGfx("big_splash", target.tile.addY(direction.delta.x), delay = 6)
                sound("grapple_splash", 3)
                exactMoveDelay(target.tile.addY(direction.delta.x), startDelay = 12, delay = 40, direction = direction)
                val hasLightSource = Light.hasLightSource(this)
                if (hasLightSource) {
                    Light.extinguish(this)
                }
                renderEmote("drowning")
                delay(2)
                exactMoveDelay(end, direction = direction)
                if (hasLightSource) {
                    message("You scramble out of the muddy water.", ChatType.Filter)
                }
                clearRenderEmote()
            }
        }

        objectOperate("Cross", "shilo_village_waterfall_stepping_stone_*") { (target) ->
            if (tile.equals(2925, 2947) || tile.equals(2925, 2951)) {
                shiloCross(target)
            }
        }

        objectApproach("Cross", "shilo_village_waterfall_stepping_stone_*") { (target) ->
            approachRange(1)
            if (tile == target.tile) {
                return@objectApproach
            }
            if (!tile.within(target.tile, 1)) {
                cantReach()
                return@objectApproach
            }
            shiloCross(target)
        }

        objectApproach("Jump-onto", "draynor_stepping_stone") { (target) ->
            approachRange(1)
            if (tile == target.tile) {
                message("You're already standing there.")
                return@objectApproach
            }
            if (!tile.within(target.tile, 1)) {
                cantReach()
                return@objectApproach
            }
            draynorCross(target)
        }

        objectOperate("Jump-onto", "draynor_stepping_stone") { (target) ->
            if (tile == target.tile) {
                // Approach incorrectly calls this after moving causing it to fire every time
                //        message("You're already standing there.")
                return@objectOperate
            }
            draynorCross(target)
        }

        objectApproach("Jump-to", "shilo_river_stepping_stone") { (target) ->
            val direction = target.tile.delta(tile).toDirection().vertical()
            walkToDelay(target.tile.addY(-(direction.delta.y * 3)))
            face(direction)
            delay()
            if (!has(Skill.Agility, 74)) {
                message("You need level 74 Agility to tackle this obstacle.")
                return@objectApproach
            }
            anim("stepping_stone_jump", delay = 15)
            face(direction)
            sound("jump", delay = 15)
            exactMoveDelay(target.tile, startDelay = 30, delay = 45, direction = direction)
            delay()
            anim("stepping_stone_jump", delay = 15)
            face(direction)
            sound("jump", delay = 15)
            exactMoveDelay(target.tile.addY(direction.delta.y * 3), startDelay = 30, delay = 45, direction = direction) // startDelta = Delta(0, -2), endDelta = Delta(0, 0)
        }
    }

    suspend fun Player.shiloCross(target: GameObject) {
        if (!has(Skill.Agility, 30)) {
            statement("The stepping stone looks very small and slippery. You'd better have an Agility level of 30.")
            return
        }
        val direction = target.tile.delta(tile).toDirection()
        message("You attempt to balance on the stepping stone.", ChatType.Filter)
        anim("stepping_stone_step", delay = 20)
        exactMoveDelay(target.tile, startDelay = 48, delay = 60, direction = direction)
        sound("jump", delay = 30)
        if (Level.success(levels.get(Skill.Agility), 51..252)) { // Unknown rate
            delay()
            message("You manage to make the jump.", ChatType.Filter)
            exp(Skill.Agility, 3.0)
        } else {
            message("You slip and fall...", ChatType.Filter)
            anim("rope_walk_fall_${if (direction == Direction.SOUTH) "left" else "right"}")
            renderEmote("drowning")
            exactMoveDelay(Tile(2928, 2949), startDelay = 52, delay = 100, direction = direction)
            areaGfx("big_splash", Tile(2928, 2949), delay = 1)
            sound("pool_plop")
            levels.drain(Skill.Prayer, multiplier = 0.5)
            walkOverDelay(Tile(2930, 2949))
            delay()
            renderEmote("swim")
            walkOverDelay(Tile(2931, if (direction == Direction.SOUTH) 2947 else 2951))

            clearRenderEmote()
            walkOverDelay(Tile(2931, if (direction == Direction.SOUTH) 2945 else 2953))

            message("You get washed up on the side of the river, after being nearly half drowned.", ChatType.Filter)
            specialAttackEnergy = (specialAttackEnergy - 100).coerceAtLeast(0)
            damage(round(levels.get(Skill.Constitution) / 5.5).toInt())
            exp(Skill.Agility, 1.0)
        }
    }

    suspend fun Player.draynorCross(target: GameObject) {
        if (!has(Skill.Agility, 31)) {
            message("You need level 31 Agility to tackle this obstacle.")
            return
        }
        delay()
        val direction = target.tile.delta(tile).toDirection()
        message("You attempt to balance on the stepping stone.", ChatType.Filter)
        anim("stepping_stone_step", delay = 30)
        if (Level.success(levels.get(Skill.Agility), 51..252)) { // Unknown rate
            exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            message("You manage to make the jump.", ChatType.Filter)
            exp(Skill.Agility, 3.0)
        } else {
            exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            delay()
            anim("fall_off_log_${if (direction == Direction.EAST) "right" else "left"}")
            message("You slip and fall...", ChatType.Filter)
            areaGfx("big_splash", target.tile.copy(y = 3362), 25)
            sound("pool_plop", delay = 25)
            exactMoveDelay(target.tile.copy(y = 3362), startDelay = 22, delay = 35, direction = direction)
            renderEmote("drowning")
            delay(2)
            renderEmote("swim")
            delay()
            if (direction == Direction.EAST) {
                walkOverDelay(target.tile.copy(y = 3362))
                walkOverDelay(Tile(3154, 3362))
                clearRenderEmote()
                walkOverDelay(Tile(3155, 3363))
            } else {
                walkOverDelay(Tile(3150, 3362))
                clearRenderEmote()
                walkOverDelay(Tile(3149, 3362))
            }
            message("You get washed up on the side of the river, after being nearly half drowned", ChatType.Filter)
            damage(20)
            exp(Skill.Agility, 1.0)
        }
    }
}
