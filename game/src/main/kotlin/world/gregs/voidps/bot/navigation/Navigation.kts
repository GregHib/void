package world.gregs.voidps.bot.navigation

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.world.interact.entity.obj.OpenDoor
import world.gregs.voidps.world.interact.world.Climb

onBot<Moved>({ it.player.mode is Movement && (it.player.mode as Movement).steps.size <= 1 }) { bot: Bot ->
    bot.resume("move")
}

onBot<OpenDoor> { bot: Bot ->
    bot.resume("move")
}

onBot<Climb> { bot: Bot ->
    bot.resume("move")
}