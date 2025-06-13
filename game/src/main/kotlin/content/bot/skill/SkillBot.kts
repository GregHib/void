package content.bot.skill

import content.bot.isBot
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.network.client.instruction.InteractDialogue

interfaceOpen("dialogue_level_up") { bot ->
    if (bot.isBot) {
        bot.instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
    }
}
