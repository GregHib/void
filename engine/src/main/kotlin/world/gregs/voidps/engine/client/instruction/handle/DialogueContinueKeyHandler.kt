package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.ContinueKey

/**
 * A slightly hacky way of doing server side dialogue continuing with key presses
 */
class DialogueContinueKeyHandler : InstructionHandler<ContinueKey>() {
    override fun validate(player: Player, instruction: ContinueKey): Boolean {
        val dialogue = player.dialogue ?: return false

        val option = if (instruction.button == -1) "continue" else "line${instruction.button}"
        if (InterfaceDefinitions.get(dialogue).components?.values?.any { it.stringId == option } == true) {
            Dialogues.continueDialogue(player, "$dialogue:$option")
            return true
        }
        return false
    }
}
