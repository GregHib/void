package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.entity.character.event.MoveStop
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.world.interact.entity.obj.OpenDoor
import world.gregs.voidps.world.interact.world.Climb

on<MoveStop> { bot: Bot ->
    if (bot.step is Walk) {
        bot.resume("move")
    }
}

on<OpenDoor> { bot: Bot ->
    bot.resume("move")
}

on<Climb> { bot: Bot ->
    bot.resume("move")
}