package world.gregs.voidps.bot.navigation

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.world.interact.entity.obj.Teleport
import world.gregs.voidps.world.interact.entity.obj.door.DoorOpened

onBot<Moved>({ (it.player.mode is Movement && it.player.steps.size <= 1) || it.player.mode == EmptyMode }) { bot: Bot ->
    bot.resume("move")
}

onBot<DoorOpened> { bot ->
    bot.resume("move")
}

onBot<Teleport> { bot ->
    bot.resume("move")
}