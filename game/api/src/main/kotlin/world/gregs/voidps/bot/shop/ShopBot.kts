package world.gregs.voidps.bot.shop

import world.gregs.voidps.bot.bot
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.bot.navigation.resume
import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("shop") { player ->
    if (player.isBot) {
        player.bot.resume("shop")
    }
}