package world.gregs.voidps.bot.skill

import world.gregs.voidps.bot.Bot
import world.gregs.voidps.bot.onBot
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.network.instruct.InteractDialogue

onBot<InterfaceOpened>({ id == "dialogue_level_up" }) { bot: Bot ->
    bot.player.instructions.tryEmit(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
}