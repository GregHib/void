package content.bot.interact.bank

import content.bot.bot
import content.bot.interact.navigation.resume
import content.bot.isBot
import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("bank") { player ->
    if (player.isBot) {
        player.bot.resume("bank")
    }
}
