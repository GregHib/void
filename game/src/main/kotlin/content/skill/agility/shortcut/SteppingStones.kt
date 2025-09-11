package content.skill.agility.shortcut

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.sound.sound
import content.skill.firemaking.Light
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
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import kotlin.math.round

@Script
class SteppingStones {

    init {
        objectApproach("Jump-across", "lumbridge_swamp_stepping_stone") {
            val direction = if (player.tile.x > target.tile.x) Direction.WEST else Direction.EAST
            val start = if (direction == Direction.WEST) Tile(3208, 9572) else Tile(3204, 9572)
            val end = if (direction == Direction.WEST) Tile(3204, 9572) else Tile(3208, 9572)
            if (player.tile != start) {
                player.walkToDelay(start)
                delay()
            }
            player.anim("stepping_stone_step", delay = 30)
            player.message(text = "You leap across with a mighty leap!", ChatType.Filter)
            player.sound("jump", delay = 35)
            player.exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            if (Level.success(player.levels.get(Skill.Agility), 51..252)) {
                player.anim("stepping_stone_step", delay = 30)
                player.sound("jump", delay = 35)
                player.exactMoveDelay(end, startDelay = 58, delay = 70, direction = direction)
                player.exp(Skill.Agility, 3.0)
            } else {
                player.message("You slip over on the slimy stone.", ChatType.Filter)
                player.anim("rope_walk_fall_down")
                areaGfx("big_splash", target.tile.addY(direction.delta.x), delay = 6)
                player.sound("grapple_splash", 3)
                player.exactMoveDelay(target.tile.addY(direction.delta.x), startDelay = 12, delay = 40, direction = direction)
                val hasLightSource = Light.hasLightSource(player)
                if (hasLightSource) {
                    Light.extinguish(player)
                }
                player.renderEmote("drowning")
                delay(2)
                player.exactMoveDelay(end, direction = direction)
                if (hasLightSource) {
                    player.message("You scramble out of the muddy water.", ChatType.Filter)
                }
                player.clearRenderEmote()
            }
        }

        objectOperate("Cross", "shilo_village_waterfall_stepping_stone_*") {
            if (player.tile.equals(2925, 2947) || player.tile.equals(2925, 2951)) {
                shiloCross()
            }
        }

        objectApproach("Cross", "shilo_village_waterfall_stepping_stone_*") {
            approachRange(1)
            if (player.tile == target.tile) {
                return@objectApproach
            }
            if (!player.tile.within(target.tile, 1)) {
                player.cantReach()
                return@objectApproach
            }
            shiloCross()
        }

        objectApproach("Jump-onto", "draynor_stepping_stone") {
            approachRange(1)
            if (player.tile == target.tile) {
                player.message("You're already standing there.")
                return@objectApproach
            }
            if (!player.tile.within(target.tile, 1)) {
                player.cantReach()
                return@objectApproach
            }
            draynorCross()
        }

        objectOperate("Jump-onto", "draynor_stepping_stone") {
            if (player.tile == target.tile) {
                // Approach incorrectly calls this after moving causing it to fire every time
                //        player.message("You're already standing there.")
                return@objectOperate
            }
            draynorCross()
        }

        objectApproach("Jump-to", "shilo_river_stepping_stone") {
            val direction = target.tile.delta(player.tile).toDirection().vertical()
            player.walkToDelay(target.tile.addY(-(direction.delta.y * 3)))
            player.face(direction)
            delay()
            if (!player.has(Skill.Agility, 74)) {
                player.message("You need level 74 Agility to tackle this obstacle.")
                return@objectApproach
            }
            player.anim("stepping_stone_jump", delay = 15)
            player.face(direction)
            player.sound("jump", delay = 15)
            player.exactMoveDelay(target.tile, startDelay = 30, delay = 45, direction = direction)
            delay()
            player.anim("stepping_stone_jump", delay = 15)
            player.face(direction)
            player.sound("jump", delay = 15)
            player.exactMoveDelay(target.tile.addY(direction.delta.y * 3), startDelay = 30, delay = 45, direction = direction) // startDelta = Delta(0, -2), endDelta = Delta(0, 0)
        }
    }

    suspend fun ObjectOption<Player>.shiloCross() {
        if (!player.has(Skill.Agility, 30)) {
            player.message("You need at least 30 Agility to do that.") // TODO proper message
            return
        }
        val direction = target.tile.delta(player.tile).toDirection()
        player.message("You attempt to balance on the stepping stone.", ChatType.Filter)
        player.anim("stepping_stone_step", delay = 20)
        player.exactMoveDelay(target.tile, startDelay = 48, delay = 60, direction = direction)
        player.sound("jump", delay = 30)
        if (Level.success(player.levels.get(Skill.Agility), 51..252)) { // Unknown rate
            delay()
            player.message("You manage to make the jump.", ChatType.Filter)
            player.exp(Skill.Agility, 3.0)
        } else {
            player.message("You slip and fall...", ChatType.Filter)
            player.anim("rope_walk_fall_${if (direction == Direction.SOUTH) "left" else "right"}")
            player.renderEmote("drowning")
            player.exactMoveDelay(Tile(2928, 2949), startDelay = 52, delay = 100, direction = direction)
            areaGfx("big_splash", Tile(2928, 2949), delay = 1)
            player.sound("pool_plop")
            player.levels.drain(Skill.Prayer, multiplier = 0.5)
            player.walkOverDelay(Tile(2930, 2949))
            delay()
            player.renderEmote("swim")
            player.walkOverDelay(Tile(2931, if (direction == Direction.SOUTH) 2947 else 2951))

            player.clearRenderEmote()
            player.walkOverDelay(Tile(2931, if (direction == Direction.SOUTH) 2945 else 2953))

            player.message("You get washed up on the side of the river, after being nearly half drowned.", ChatType.Filter)
            player.specialAttackEnergy = (player.specialAttackEnergy - 100).coerceAtLeast(0)
            player.damage(round(player.levels.get(Skill.Constitution) / 5.5).toInt())
            player.exp(Skill.Agility, 1.0)
        }
    }

    suspend fun ObjectOption<Player>.draynorCross() {
        if (!player.has(Skill.Agility, 31)) {
            player.message("You need level 31 Agility to tackle this obstacle.")
            return
        }
        delay()
        val direction = target.tile.delta(player.tile).toDirection()
        player.message("You attempt to balance on the stepping stone.", ChatType.Filter)
        player.anim("stepping_stone_step", delay = 30)
        if (Level.success(player.levels.get(Skill.Agility), 51..252)) { // Unknown rate
            player.exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            player.message("You manage to make the jump.", ChatType.Filter)
            player.exp(Skill.Agility, 3.0)
        } else {
            player.exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            delay()
            player.anim("fall_off_log_${if (direction == Direction.EAST) "right" else "left"}")
            player.message("You slip and fall...", ChatType.Filter)
            areaGfx("big_splash", target.tile.copy(y = 3362), 25)
            player.sound("pool_plop", delay = 25)
            player.exactMoveDelay(target.tile.copy(y = 3362), startDelay = 22, delay = 35, direction = direction)
            player.renderEmote("drowning")
            delay(2)
            player.renderEmote("swim")
            delay()
            if (direction == Direction.EAST) {
                player.walkOverDelay(target.tile.copy(y = 3362))
                player.walkOverDelay(Tile(3154, 3362))
                player.clearRenderEmote()
                player.walkOverDelay(Tile(3155, 3363))
            } else {
                player.walkOverDelay(Tile(3150, 3362))
                player.clearRenderEmote()
                player.walkOverDelay(Tile(3149, 3362))
            }
            player.message("You get washed up on the side of the river, after being nearly half drowned", ChatType.Filter)
            player.damage(20)
            player.exp(Skill.Agility, 1.0)
        }
    }
}
