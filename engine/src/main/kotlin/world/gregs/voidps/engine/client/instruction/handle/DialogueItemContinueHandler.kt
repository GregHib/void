package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InteractDialogueItem

class DialogueItemContinueHandler(
    private val definitions: ItemDefinitions,
) : InstructionHandler<InteractDialogueItem>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractDialogueItem) {
        val definition = definitions.getOrNull(instruction.item)
        if (definition == null) {
            logger.debug { "Item ${instruction.item} not found for player $player." }
            return
        }
        Dialogues.continueItem(player, definition.stringId)
    }
}
