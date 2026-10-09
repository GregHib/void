package content.area.fremennik_province.rellekka

import content.entity.combat.hit.directHit
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

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
            val success = Settings["agility.disableCourseFailure", false] || Level.success(levels.get(Skill.Agility), 1..254)
            if (success) {
                anim("agilty_shortcut_jump")
            } else {
                anim("agilty_shortcut_jump_fail")
            }
            exactMoveDelay(start.addX(direction.delta.x * 7), direction = direction, startDelay = 30, delay = 120)
            if (success) {
                message("Your feet skid as you land on the floor.")
            } else {
                directHit(random.nextInt(70, 90)) // TODO proper values
                message("You land badly and take some damage.")
            }
            exp(Skill.Agility, 5.0)
        }
    }
}
