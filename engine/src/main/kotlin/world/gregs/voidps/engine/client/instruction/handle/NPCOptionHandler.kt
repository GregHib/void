package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.handle.ObjectOptionHandler.Companion.getDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.network.instruct.InteractNPC

class NPCOptionHandler(
    private val npcs: NPCs,
    private val definitions: NPCDefinitions
) : InstructionHandler<InteractNPC>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractNPC) {
        val npc = npcs.indexed(instruction.npcIndex) ?: return
        val definition = getDefinition(player, definitions, npc.def, npc.def)
        val options = definition.options
        val index = instruction.option - 1
        val selectedOption = options.getOrNull(index)
        if (selectedOption == null) {
            player.noInterest()
            logger.warn { "Invalid npc option $npc $index" }
            return
        }
        if (selectedOption == "Listen-to" && player["movement", "walk"] == "music") {
            player.message("You are already resting.")
            return
        }
        if (selectedOption == "Talk-to") {
            player.talkWith(npc)
        }
        val range = if (selectedOption == "Attack") player["attack_range", 1] else null
        player.mode = Interact(player, npc, NPCOption(player, npc, definition, selectedOption), approachRange = range)
    }

}