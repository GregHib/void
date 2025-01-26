package content.bot.interact.navigation

import content.bot.bot
import content.bot.isBot
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.onEvent
import content.entity.obj.door.DoorOpened
import content.entity.obj.teleport

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