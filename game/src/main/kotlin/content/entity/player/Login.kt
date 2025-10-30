package content.entity.player

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.flagMovementType
import world.gregs.voidps.engine.entity.character.player.flagTemporaryMoveType

class Login : Script {

    init {
        playerSpawn { player ->
            if (player.isBot) {
                return@playerSpawn
            }
            player.options.send(2)
            player.options.send(4)
            player.options.send(7)
            player.flagTemporaryMoveType()
            player.flagMovementType()
            player.flagAppearance()
            player.clearFace()
        }
    }
}
