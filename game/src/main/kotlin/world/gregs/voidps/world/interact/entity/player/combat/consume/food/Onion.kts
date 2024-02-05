package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume({ item.id == "onion" }) { player: Player ->
    player.message("It hurts to see a grown ${if (player.male) "male" else "female"} cry.", ChatType.Filter)
}