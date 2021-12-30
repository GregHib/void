import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.cooking.Consume

on<Consume>({ item.id == "onion" }) { player: Player ->
    player.message("It hurts to see a grown ${if (player.appearance.male) "male" else "female"} cry.", ChatType.GameFilter)
}