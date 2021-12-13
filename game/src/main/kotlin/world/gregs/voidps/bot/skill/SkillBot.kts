import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.InteractDialogue

on<InterfaceOpened>({ id == "dialogue_level_up" }) { bot: Bot ->
    bot.player.instructions.tryEmit(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
}