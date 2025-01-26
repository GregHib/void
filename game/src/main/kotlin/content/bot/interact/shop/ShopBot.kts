package content.bot.interact.shop

import content.bot.bot
import content.bot.isBot
import content.bot.interact.navigation.resume
import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("shop") { player ->
    if (player.isBot) {
        player.bot.resume("shop")
    }
}