package content.bot.skill

import content.bot.isBot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.network.client.instruction.InteractDialogue

class SkillBot : Script {
    init {
        interfaceOpened("dialogue_level_up") {
            if (isBot) {
                instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
            }
        }
    }
}
