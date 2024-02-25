package world.gregs.voidps.bot.bank

import world.gregs.voidps.bot.bot
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("bank") { player ->
    if (player.isBot) {
        player.bot.resume("bank")
    }
}