package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.Script
@Script
class Onion {

    init {
        consume("onion") { player ->
            player.message("It hurts to see a grown ${if (player.male) "male" else "female"} cry.", ChatType.Filter)
        }

    }

}
