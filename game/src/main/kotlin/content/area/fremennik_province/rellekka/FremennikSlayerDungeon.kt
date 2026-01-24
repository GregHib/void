package content.area.fremennik_province.rellekka

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class FremennikSlayerDungeon : Script {
    init {
        objectOperate("Read", "slayer_danger_sign") {
            statement("<red>WARNING!<br>This area contains very dangerous creatures!<br>Do not pass unless properly prepared!")
        }

        objectOperate("Squeeze-through", "slayer_dungeon_crevice") { (target) ->
            if (!has(Skill.Agility, 62)) {
                message("You need level 62 agility in order to contort your body through this crack.")
                return@objectOperate
            }
            val direction = if (tile.x < 2734) Direction.EAST else Direction.WEST
            face(direction)
            anim("agility_shortcut_crack_enter")
            exactMoveDelay(target.tile, direction = direction)
            anim("agilty_shortcut_tunnel_walk")
            exactMoveDelay(target.tile.addX(direction.delta.x * 3), direction = direction)
            anim("agility_shortcut_crack_leave")
            exactMoveDelay(target.tile.addX(direction.delta.x * 4), direction = direction)
            message("You climb your way through the narrow crevice.")
            // https://youtu.be/KrTaJcIfaT0?t=2
            exp(Skill.Agility, 7.5) // https://youtu.be/DQx_Dmc12O4?t=30
        }

        objectOperate("Jump-across", "slayer_dungeon_chasm") { (target) ->
            if (!has(Skill.Agility, 81)) {
                // https://youtu.be/91YaxnEa81k?t=334
                message("You need an agility level of 81 to tackle this obstacle.")
                return@objectOperate
            }
            // https://youtu.be/xVgEzolS6eI?t=73
            val direction = if (tile.x < 2771) Direction.EAST else Direction.WEST
            val start = if (direction == Direction.EAST) Tile(2768, 10002) else Tile(2775, 10002)
            walkToDelay(start)
            face(direction)
            delay()
            anim("agilty_shortcut_jump")
            exactMoveDelay(start.addX(direction.delta.x * 7), direction = direction, startDelay = 30, delay = 120)
            message("Your feet skid as you land on the floor.")
            exp(Skill.Agility, 10.0) // TODO proper amount
        }
    }
}