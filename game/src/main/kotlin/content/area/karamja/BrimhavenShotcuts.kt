package content.area.karamja

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class BrimhavenShotcuts : Script {

    val directions = mutableListOf(Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH)

    init {
        objectOperate("Swing-on", "brimhaven_ropeswing_west") { (target) ->
            face(target.tile.add(Direction.WEST))
            if (!has(Skill.Agility, 10)) {
                message("You need an agility level of 10 to attempt to swing on this vine.")
                return@objectOperate
            }
            walkToDelay(Tile(2709, 3209))
            anim("rope_swing")
            target.anim("swing_rope")
            exactMoveDelay(Tile(2704, 3209), startDelay = 45, delay = 70, direction = Direction.WEST)
            exp(Skill.Agility, 3.0)
            message("You skillfully swing across.", ChatType.Filter)
        }

        objectOperate("Swing-on", "brimhaven_ropeswing_east") { (target) ->
            face(target.tile.add(Direction.EAST))
            if (!has(Skill.Agility, 10)) {
                message("You need an agility level of 10 to attempt to swing on this vine.")
                return@objectOperate
            }
            walkToDelay(Tile(2705, 3205))
            anim("rope_swing")
            target.anim("swing_rope")
            exactMoveDelay(Tile(2709, 3205), startDelay = 45, delay = 70, direction = Direction.EAST)
            exp(Skill.Agility, 3.0)
            message("You skillfully swing across.", ChatType.Filter)
        }

        objectOperate("Jump-from", "brimhaven_stepping_stones_start") { (target) ->
            face(target.tile.add(Direction.WEST))
            if (!has(Skill.Agility, 12)) {
                message("You need an agility level of 12 to attempt to swing on this vine.")
                return@objectOperate
            }
            message("You carefully start crossing the stepping stones...", ChatType.Filter)
            for (direction in directions) {
                anim("stepping_stone_jump")
                exactMoveDelay(tile.add(direction), direction = direction)
                face(direction)
                delay()
            }
            exp(Skill.Agility, 7.5)
            message("... You safely cross to the other side.", ChatType.Filter)
        }

        objectOperate("Jump-from", "brimhaven_stepping_stones_end") { (target) ->
            face(target.tile.add(Direction.EAST))
            message("You carefully start crossing the stepping stones...", ChatType.Filter)
            for (i in directions.indices.reversed()) {
                val direction = directions[i].inverse()
                anim("stepping_stone_jump")
                exactMoveDelay(tile.add(direction), direction = direction)
                face(direction)
                delay()
            }
            exp(Skill.Agility, 7.5)
            message("... You safely cross to the other side.", ChatType.Filter)
        }

        objectOperate("Walk-across", "brimhaven_log_balance_start") { (target) ->
            walkAcross(target, 30, 2687, 10.0)
        }

        objectOperate("Walk-across", "brimhaven_log_balance_end") { (target) ->
            walkAcross(target, 30, 2682, 10.0)
        }
    }

    suspend fun Player.walkAcross(target: GameObject, level: Int, targetX: Int, exp: Double) {
        if (!has(Skill.Agility, level)) {
            message("You need an Agility level of $level to cross the log.")
            return
        }
        message("You walk carefully across the slippery log...", ChatType.Filter)
        renderEmote("rope_balance")
        walkOverDelay(target.tile)
        walkOverDelay(target.tile.copy(targetX))
        clearRenderEmote()
        exp(Skill.Agility, exp)
        message("... and make it safely to the other side.", ChatType.Filter)
    }
}
