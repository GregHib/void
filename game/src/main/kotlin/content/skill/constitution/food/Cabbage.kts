package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

consume("cabbage") { player ->
    player.message("You don't really like it much.", ChatType.Filter)
}
