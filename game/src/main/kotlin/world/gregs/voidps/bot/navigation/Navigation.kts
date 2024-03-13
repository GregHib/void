package world.gregs.voidps.bot.navigation

import world.gregs.voidps.bot.bot
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.world.interact.entity.obj.door.DoorOpened
import world.gregs.voidps.world.interact.entity.obj.teleport

move({ (player.mode is Movement && player.steps.size <= 1) || player.mode == EmptyMode }) { player ->
    if (player.isBot) {
        player.bot.resume("move")
    }
}

onEvent<Player, DoorOpened> { player ->
    if (player.isBot) {
        player.bot.resume("move")
    }
}

teleport {
    if (player.isBot) {
        player.bot.resume("move")
    }
}