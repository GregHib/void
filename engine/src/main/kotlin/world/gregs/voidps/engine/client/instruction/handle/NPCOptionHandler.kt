package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.instruction.handle.ObjectOptionHandler.Companion.getDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.network.client.instruction.InteractNPC

class NPCOptionHandler(
    private val npcs: NPCs,
    private val definitions: NPCDefinitions,
) : InstructionHandler<InteractNPC>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: InteractNPC) {
        if (player.contains("delay")) {
            return
        }
        val npc = npcs.indexed(instruction.npcIndex) ?: return
        var def = npc.def
        val transform = npc["transform_id", ""]
        if (transform.isNotBlank()) {
            def = definitions.get(transform)
        }
        val definition = getDefinition(player, definitions, def, def)
        val options = definition.options
        val index = instruction.option - 1
        val selectedOption = options.getOrNull(index)
        if (selectedOption == null) {
            player.noInterest()
            logger.warn { "Invalid npc option $npc $index ${options.contentToString()}" }
            return
        }
        if (selectedOption == "Listen-to" && player["movement", "walk"] == "music") {
            player.message("You are already resting.")
            return
        }
        if (player.hasClock("stunned")) {
            player.message("You're stunned!", ChatType.Filter)
            return
        }
        player.closeInterfaces()
        player.talkWith(npc, definition)
        player.interactNpc(npc, selectedOption, definition)
    }
}

fun Character.interactNpc(target: NPC, option: String, definition: NPCDefinition = if (this is Player) target.def(this) else target.def) {
    mode = Interact(this, target, NPCOption(this, target, definition, option))
}
