package world.gregs.voidps.bot.bank

import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened

onBot<InterfaceOpened>({ id == "bank" }) { bot ->
    bot.resume("bank")
}