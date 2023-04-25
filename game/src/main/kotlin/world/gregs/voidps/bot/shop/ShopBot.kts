import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened

onBot<InterfaceOpened>({ id == "shop" }) { bot: Bot ->
    bot.resume("shop")
}