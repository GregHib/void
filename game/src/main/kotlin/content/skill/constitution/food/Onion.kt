package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.male

class Onion : Script {

    init {
        consumed("onion") { _, _ ->
            message("It hurts to see a grown ${if (male) "male" else "female"} cry.", ChatType.Filter)
        }
    }
}
