package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class Cabbage : Script {

    init {
        consumed("cabbage") { _, _ ->
            message("You don't really like it much.", ChatType.Filter)
        }
    }
}
