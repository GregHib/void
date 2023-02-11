package world.gregs.voidps.world.activity.combat.consume.food

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Consume>({ item.id == "cabbage" }) { player: Player ->
    player.message("You don't really like it much.", ChatType.Filter)
}