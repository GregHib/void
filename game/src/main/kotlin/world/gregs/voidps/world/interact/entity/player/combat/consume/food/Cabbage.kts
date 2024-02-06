package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("cabbage") { player: Player ->
    player.message("You don't really like it much.", ChatType.Filter)
}