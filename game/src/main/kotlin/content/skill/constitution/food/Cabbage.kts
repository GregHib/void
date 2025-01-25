package content.skill.constitution.food

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import content.skill.constitution.consume

consume("cabbage") { player ->
    player.message("You don't really like it much.", ChatType.Filter)
}