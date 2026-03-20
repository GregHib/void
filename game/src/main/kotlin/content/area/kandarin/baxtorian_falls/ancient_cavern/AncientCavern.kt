package content.area.kandarin.baxtorian_falls.ancient_cavern

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class AncientCavern : Script {
    init {
        objectApproach("Dive in", "ancient_cavern_whirlpool") {
            walkToDelay(Tile(tile.x.coerceIn(2511..2512), 3516))
            face(Direction.SOUTH)
            delay(1)
            anim("jump_whirlpool")
            exactMove(Tile(2512, 3508), startDelay = 15, delay = 255, direction = Direction.SOUTH)
            delay(4)
            open("fade_out")
            delay(4)
            message("You dive into the swirling maelstrom of the whirlpool.", type = ChatType.Filter)
            tele(1763, 5365, 1)
            delay(1)
            message("You are swirled beneath the water, the darkness and pressure are overwhelming.", type = ChatType.Filter)
            delay(1)
            message("Mystical forces guide you into a cavern below the whirlpool.", type = ChatType.Filter)
            delay(3)
            open("fade_in")
        }
    }
}