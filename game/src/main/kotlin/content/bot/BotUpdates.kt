package content.bot

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractDialogue

/**
 * Listen for state changes which would change which activities are available to a bot
 */
class BotUpdates : Script {
    val logger = InlineLogger()

    init {
        /*
            Track state changes to re-evaluate available activities
         */
        levelChanged { skill, _, _ ->
            if (isBot) {
                bot.evaluate.add("skill:${skill.name.lowercase()}")
            }
        }

        inventoryUpdated { inventory, _ ->
            if (isBot) {
                bot.evaluate.add("inv:$inventory")
            }
        }

        entered("*") {
            if (isBot) {
                resetTimeout("area:${it.name}")
                bot.evaluate.add("area:${it.name}")
            }
        }

        variableSet { key, from, to ->
            if (isBot && from != to) {
                bot.evaluate.add("var:$key")
                resetTimeout("variable:$key")
            }
        }

        itemAdded(inventory = "inventory") {
            resetTimeout("item:${it.item.id}")
        }

        experience { skill, _, _ ->
            resetTimeout("skill:${skill.name.lowercase()}")
        }

        // Close level-up dialogues
        interfaceOpened("dialogue_level_up") {
            if (isBot) {
                instructions.trySend(InteractDialogue(interfaceId = 740, componentId = 3, option = -1))
            }
        }
    }

    /**
     * Reset timeout when produce has been produced
     */
    private fun Player.resetTimeout(key: String) {
        if (!isBot || bot.noTask()) {
            return
        }
        val frame = bot.frame()
        val produces = frame.behaviour.produces
        if (produces.contains(key)) {
            frame.timeout = 0
        }
    }
}
