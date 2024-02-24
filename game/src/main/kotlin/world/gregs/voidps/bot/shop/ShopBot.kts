package world.gregs.voidps.bot.shop

import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened

onBot<InterfaceOpened>({ id == "shop" }) { bot ->
    bot.resume("shop")
}