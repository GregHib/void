package content.bot

import world.gregs.voidps.engine.Script
import world.gregs.voidps.network.client.instruction.InteractDialogue

/**
 * Listen for state changes which would change which activities are available to a bot
 */
class BotUpdates(val manager: BotManager) : Script {
    init {
        levelChanged { skill, _, _ ->
            if (isBot) {
                manager.update(bot, "skill:${skill.name.lowercase()}")
            }
        }

//        moved { from ->
//            if (isBot && tile != from) {
//                manager.update(bot, "tile")  // FIXME expensive
//            }
//        }

        variableSet { key, from, to ->
            if (isBot && from != to) {
                manager.update(bot, "var:$key")
            }
        }

        inventoryUpdated { inventory, _ ->
            if (isBot) {
                manager.update(bot, "inv:$inventory")
            }
        }

        entered("*") {
            if (isBot) {
                manager.update(bot, "enter:${it.name}")
            }
        }

        interfaceOpened("dialogue_level_up") {
            if (isBot) {
                instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
            }
        }
    }
}