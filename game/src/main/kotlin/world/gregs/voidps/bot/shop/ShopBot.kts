import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "shop" }) { bot: Bot ->
    bot.resume("shop")
}