package world.gregs.voidps.bot.navigation

import world.gregs.voidps.bot.bot
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.obj.Teleport
import world.gregs.voidps.world.interact.entity.obj.door.DoorOpened

on<Moved>({ (player.mode is Movement && player.steps.size <= 1) || player.mode == EmptyMode }) { player ->
    if (player.isBot) {
        player.bot.resume("move")
    }
}

on<DoorOpened> { player ->
    if (player.isBot) {
        player.bot.resume("move")
    }
}

on<Teleport> { player ->
    if (player.isBot) {
        player.bot.resume("move")
    }
}