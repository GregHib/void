package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.MoveStop
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.instruct.Walk

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

on<ActionFinished>({ type == ActionType.Climb || type == ActionType.OpenDoor }) { bot: Bot ->
    if (bot.step is InteractObject) {
        bot.resume("move")
    }
}