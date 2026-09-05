package content.skill.agility.shortcut

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

class LogBalance : Script {

    init {
        objectOperate("Walk-across", "coal_truck_log_balance") { (target) ->
            if (!has(Skill.Agility, 20)) {
                message("You need at least 20 Agility to do that.") // TODO proper message
                return@objectOperate
            }
            message("You walk carefully across the slippery log...", ChatType.Filter)
            renderEmote("rope_balance")
            walkOverDelay(target.tile)
            if (target.tile.equals(2602, 3477)) {
                walkOverDelay(target.tile.copy(2598))
            } else if (target.tile.equals(2599, 3477)) {
                walkOverDelay(target.tile.copy(2603))
            }
            clearRenderEmote()
            exp(Skill.Agility, 8.5)
            message("... and make it safely to the other side.", ChatType.Filter)
        }

        objectApproach("Cross", "isafdar_log_balance,isafdar_log_balance_2,arandar_log_balance", arrive = false) { (target) ->
            approachRange(10)
            steps.clear()
            crossLog(target)
        }

        objectOperate("Cross", "isafdar_log_balance,isafdar_log_balance_2,arandar_log_balance") { (target) ->
            crossLog(target)
        }

        objectOperate("Walk-across", "ardougne_log_balance_east,ardougne_log_balance_west") { (target) ->
            if (!has(Skill.Agility, 33)) {
                message("You need an Agility level of 33 to negotiate this obstacle.")
                return@objectOperate
            }
            val direction = if (target.id.removePrefix("ardougne_log_balance_") == "east") Direction.WEST else Direction.EAST
            message("You attempt to walk across the slippery log.", ChatType.Filter)
            renderEmote("rope_balance")
            walkOverDelay(target.tile)
            val middle = target.tile.add(direction)
            walkOverDelay(middle)
            if (Level.success(levels.get(Skill.Agility), 90..250)) {
                walkOverDelay(middle.add(direction.delta.x * 2))
                message("You make it across the log without any problems.")
                clearRenderEmote()
                delay()
                exp(Skill.Agility, 4.0)
            } else {
                message("You lose your footing and fall into the water.")
                anim("fall_off_log_left")
                sound("stumble_loop", repeat = 10)
                exactMove(middle.addY(-1), startDelay = 22, delay = 35, direction = direction)
                delay()
                renderEmote("swim")
                areaGfx("big_splash", middle.addY(-2), delay = 3)
                sound("pool_plop")
                message("You're being washed down the river.")
                walkOverDelay(middle.addY(-1))
                message("You feel like you're drowning...", ChatType.Filter)
                walkOverDelay(middle.addY(-6))
                walkOverDelay(middle.addY(-7).add(direction))
                message("You finally come to shore.", ChatType.Filter)
                clearRenderEmote()
                walkOverDelay(middle.addY(-7).add(direction.delta.x * 2))
                damage(random.nextInt(20, 41))
                exp(Skill.Agility, 2.0)
            }
        }
    }

    suspend fun Player.crossLog(target: GameObject) {
        if (!has(Skill.Agility, 45)) {
            message("You need an Agility level of 45 to negotiate this obstacle.")
            return
        }
        val (start, end) = when (target.id) {
            "isafdar_log_balance" -> Tile(2202, 3237) to Tile(2196, 3237)
            "isafdar_log_balance_2" -> Tile(2258, 3250) to Tile(2264, 3250)
            else -> Tile(2290, 3232) to Tile(2290, 3239)
        }
        val from = if (tile.distanceTo(start) <= tile.distanceTo(end)) start else end
        val to = if (from == start) end else start
        // Approach the log at normal speed; walking away cancels
        walkTo(from)
        var count = 0
        while (tile != from && count++ < 50) {
            pause(1)
        }
        if (tile != from) {
            return
        }
        message("You walk carefully across the slippery log...", ChatType.Filter)
        delay()
        renderEmote("rope_balance")
        walkTo(to, noCollision = true, forceWalk = true)
        var steps = 0
        while (tile != to && steps++ < 10) {
            sound("log_balance")
            delay()
        }
        clearRenderEmote()
        exp(Skill.Agility, 7.5)
        message("... and make it safely to the other side.", ChatType.Filter)
    }
}
