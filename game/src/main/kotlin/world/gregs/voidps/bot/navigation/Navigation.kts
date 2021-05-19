package world.gregs.voidps.bot.navigation

import world.gregs.voidps.bot.task
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.utility.inject

on<MoveStop> { bot: Bot ->
    if (bot.step is Walk) {
        bot.resume("move")
    }
}

on<ActionFinished>({ type == ActionType.Climb || type == ActionType.OpenDoor }) { bot: Bot ->
    if (bot.step is InteractObject) {
        bot.resume("move")
    }
}

val areas: Areas by inject()

task { bot ->
//    areas.getTagged("bank")
    bot.goToNearest("bank")
//    bot.goToArea(areas.getTagged("bank").first())
    println("Task complete")
}