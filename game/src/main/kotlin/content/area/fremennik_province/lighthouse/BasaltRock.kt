package content.area.fremennik_province.lighthouse

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

class BasaltRock : Script {

    init {
        obstacle("Jump-to", "beach", Tile(2522, 3595), Direction.NORTH, exp = false)

        obstacle("Jump-across", "basalt_rock_start", Tile(2522, 3597), Direction.SOUTH, exp = false)

        obstacle("Jump-across", "basalt_rock_2", Tile(2522, 3600), Direction.NORTH, exp = true)

        obstacle("Jump-across", "basalt_rock_3", Tile(2522, 3602), Direction.SOUTH, exp = true)

        obstacle("Jump-across", "basalt_rock_4", Tile(2518, 3611), Direction.WEST, exp = true)

        obstacle("Jump-across", "basalt_rock_5", Tile(2516, 3611), Direction.EAST, exp = true)

        obstacle("Jump-across", "basalt_rock_6", Tile(2514, 3613), Direction.NORTH, exp = true)

        obstacle("Jump-across", "basalt_rock_7", Tile(2514, 3615), Direction.SOUTH, exp = true)

        obstacle("Jump-across", "basalt_rock_end", Tile(2514, 3617), Direction.NORTH, exp = false)

        obstacle("Jump-to", "rocky_shore", Tile(2514, 3619), Direction.SOUTH, exp = false)
    }

    suspend fun jump(player: Player, target: GameObject, opposite: Tile, direction: Direction, exp: Boolean) {
        player.walkToDelay(target.tile)
        player.clear("face_entity")
        // Fail on jump
        val fail = when {
            player.tile.equals(2522, 3600) -> Tile(2521, 3596)
            player.tile.equals(2514, 3615) -> Tile(2515, 3618)
            else -> null
        }
        if (fail == null || Level.success(player.levels.get(Skill.Agility), 5..255)) {
            player.anim("stepping_stone_step", delay = 19)
            player.sound("jump", delay = 35)
            player.exactMoveDelay(opposite, startDelay = 47, delay = 59, direction = direction)
            if (exp) {
                player.exp(Skill.Agility, 2.0)
            }
        } else {
            player.message("You slip on the slimy causeway.")
            player.anim("rope_walk_fall_left")
            val fall = player.tile.copy(x = fail.x)
            player.exactMoveDelay(fall, startDelay = 33, delay = 53, direction = direction)
            player.renderEmote("swim")
            areaGfx("big_splash", fall, delay = 3)
            player.sound("pool_plop")
            player.walkOverDelay(fail)
            player.message("The tide sweeps you back to shore.")
            player.clearRenderEmote()
            player.walkOverDelay(fail.add(direction.inverse()))
            player.damage(random.nextInt(100))
            if (exp) {
                player.exp(Skill.Agility, 0.5)
            }
        }
    }

    fun obstacle(option: String, rock: String, tile: Tile, direction: Direction, exp: Boolean) {
        objectOperate(option, rock) { (target) ->
            jump(this, target, tile.add(direction).add(direction), direction, exp)
        }

        objectApproach(option, rock) { (target) ->
            val sameSide = when (direction) {
                Direction.NORTH -> this.tile.y <= target.tile.y
                Direction.EAST -> this.tile.x <= target.tile.x
                Direction.SOUTH -> this.tile.y >= target.tile.y
                Direction.WEST -> this.tile.x >= target.tile.x
                else -> false
            }
            if (sameSide) {
                jump(this, target, tile.add(direction).add(direction), direction, exp)
            } else {
                jump(this, target, target.tile, direction.inverse(), exp)
            }
        }
    }
}
