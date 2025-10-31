package content.skill.agility.shortcut

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.chat.obstacle
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction

class Stiles : Script {

    init {
        objectOperate("Climb-over", "freds_farm_stile") { (target) ->
            climbStile(target, Direction.NORTH)
        }

        objectOperate("Climb-over", "catherby_stile") { (target) ->
            climbStile(target, Direction.NORTH)
        }

        objectOperate("Climb-over", "death_plateau_stile") { (target) ->
            climbStile(target, Direction.NORTH)
        }

        objectOperate("Climb-over", "falconry_area_stile") { (target) ->
            climbStile(target, Direction.NORTH)
        }

        objectOperate("Climb-over", "ardougne_farm_stile") { (target) ->
            climbStile(target, Direction.EAST)
        }

        objectOperate("Climb-over", "falador_farm_stile") { (target) ->
            val rotation = when (target.rotation) {
                2 -> Direction.NORTH
                3 -> Direction.EAST
                else -> return@objectOperate noInterest()
            }
            climbStile(target, rotation)
        }

        objectOperate("Climb-over", "vinesweeper_stile") { (target) ->
            val rotation = when (target.rotation) {
                0, 2 -> Direction.NORTH
                1, 3 -> Direction.EAST
                else -> return@objectOperate noInterest()
            }
            climbStile(target, rotation)
        }

        objectOperate("Climb-over", "falador_crumbling_wall") { (target) ->
            if (!has(Skill.Agility, 5)) {
                obstacle(5)
                return@objectOperate
            }
            climbStile(target, Direction.EAST)
            exp(Skill.Agility, 0.5)
        }

        objectOperate("Squeeze-through", "mcgrubor_wood_railing") { (target) ->
            climbStile(target, Direction.WEST, "railing_squeeze")
        }
    }

    suspend fun Player.climbStile(target: GameObject, rotation: Direction, anim: String = "rocks_pile_climb") {
        val direction = when (rotation) {
            Direction.NORTH -> if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            Direction.SOUTH -> if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            Direction.EAST -> if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
            Direction.WEST -> if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            else -> return noInterest()
        }
        val start = if (direction == rotation) target.tile else target.tile.minus(direction)
        walkOverDelay(start)
        face(direction)
        delay()
        anim(anim)
        val target = if (direction == rotation) target.tile.add(direction) else target.tile
        exactMoveDelay(target, 30, direction = direction)
    }
}
