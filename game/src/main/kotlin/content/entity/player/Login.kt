package content.entity.player

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.flagMovementType
import world.gregs.voidps.engine.entity.character.player.flagTemporaryMoveType

class Login : Script {

    init {
        playerSpawn {
            if (isBot) {
                return@playerSpawn
            }
            options.send(2)
            options.send(4)
            options.send(7)
            flagTemporaryMoveType()
            flagMovementType()
            flagAppearance()
            clearFace()
        }
    }
}
