package world.gregs.voidps.bot.skill

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.network.client.instruction.InteractDialogue

interfaceOpen("dialogue_level_up") { bot ->
    bot.instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
}