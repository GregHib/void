package content.bot.interact.bank

import content.bot.bot
import content.bot.isBot
import content.bot.interact.navigation.resume
import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("bank") { player ->
    if (player.isBot) {
        player.bot.resume("bank")
    }
}