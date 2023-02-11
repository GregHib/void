package world.gregs.voidps.bot.navigation

import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.obj.OpenDoor
import world.gregs.voidps.world.interact.world.Climb

on<Moved>({ it.player.mode is Movement && (it.player.mode as Movement).steps.size <= 1 }) { bot: Bot ->
    bot.resume("move")
}

on<OpenDoor> { bot: Bot ->
    bot.resume("move")
}

on<Climb> { bot: Bot ->
    bot.resume("move")
}