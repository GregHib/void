package content.skill.agility.shortcut

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.sound.sound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

objectOperate("Walk-across", "coal_truck_log_balance") {
    if (!player.has(Skill.Agility, 20)) {
        player.message("You need at least 20 Agility to do that.") // TODO proper message
        return@objectOperate
    }
    player.message("You walk carefully across the slippery log...", ChatType.Filter)
    player.renderEmote("rope_balance")
    player.walkOverDelay(target.tile)
    if (target.tile.equals(2602, 3477)) {
        player.walkOverDelay(target.tile.copy(2598))
    } else if (target.tile.equals(2599, 3477)) {
        player.walkOverDelay(target.tile.copy(2603))
    }
    player.clearRenderEmote()
    player.exp(Skill.Agility, 8.5)
    player.message("... and make it safely to the other side.", ChatType.Filter)
}

objectOperate("Walk-across", "ardougne_log_balance_east", "ardougne_log_balance_west") {
    if (!player.has(Skill.Agility, 33)) {
        player.message("You need an Agility level of 33 to negotiate this obstacle.")
        return@objectOperate
    }
    val direction = if (target.id.removePrefix("ardougne_log_balance_") == "east") Direction.WEST else Direction.EAST
    player.message("You attempt to walk across the slippery log.", ChatType.Filter)
    player.renderEmote("rope_balance")
    player.walkOverDelay(target.tile)
    val middle = target.tile.add(direction)
    player.walkOverDelay(middle)
    if (Level.success(player.levels.get(Skill.Agility), 90..250)) {
        player.walkOverDelay(middle.add(direction.delta.x * 2))
        player.message("You make it across the log without any problems.")
        player.clearRenderEmote()
        delay()
        player.exp(Skill.Agility, 4.0)
    } else {
        player.message("You lose your footing and fall into the water.")
        player.anim("fall_off_log_left")
        player.sound("stumble_loop", repeat = 10)
        player.exactMove(middle.addY(-1), startDelay = 22, delay = 35, direction = direction)
        delay()
        player.renderEmote("swim")
        areaGfx("big_splash", middle.addY(-2), delay = 3)
        player.sound("pool_plop")
        player.message("You're being washed down the river.")
        player.walkOverDelay(middle.addY(-1))
        player.message("You feel like you're drowning...", ChatType.Filter)
        player.walkOverDelay(middle.addY(-6))
        player.walkOverDelay(middle.addY(-7).add(direction))
        player.message("You finally come to shore.", ChatType.Filter)
        player.clearRenderEmote()
        player.walkOverDelay(middle.addY(-7).add(direction.delta.x * 2))
        player.damage(random.nextInt(20, 41))
        player.exp(Skill.Agility, 2.0)
    }
}
