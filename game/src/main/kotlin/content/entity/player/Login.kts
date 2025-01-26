package content.entity.player

import content.bot.isBot
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.flagMovementType
import world.gregs.voidps.engine.entity.character.player.flagTemporaryMoveType
import world.gregs.voidps.engine.entity.playerSpawn

playerSpawn { player ->
    if (!player.isBot) {
        player.options.send(2)
        player.options.send(4)
        player.options.send(7)
        player.flagMovementType()
        player.flagTemporaryMoveType()
        player.flagAppearance()
        player.clearFace()
    }
}