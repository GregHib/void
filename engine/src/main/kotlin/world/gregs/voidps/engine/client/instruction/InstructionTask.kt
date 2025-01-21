package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects

class InstructionTask(
    private val players: Players,
    npcs: NPCs,
    items: FloorItems,
    objects: GameObjects,
    itemDefinitions: ItemDefinitions,
    objectDefinitions: ObjectDefinitions,
    npcDefinitions: NPCDefinitions,
    interfaceDefinitions: InterfaceDefinitions,
    handler: InterfaceHandler
) : Runnable {

    private val logger = InlineLogger()
    private val handlers = InstructionHandlers(
        players,
        npcs,
        items,
        objects,
        itemDefinitions,
        objectDefinitions,
        npcDefinitions,
        interfaceDefinitions,
        handler
    )

    override fun run() {
        for (player in players) {
            for (i in 0 until MAX_INSTRUCTIONS) {
                val instruction = player.instructions.tryReceive().getOrNull() ?: break
                if (player["debug", false]) {
                    logger.debug { "${player.accountName} ${player.tile} - $instruction" }
                }
                try {
                    handlers.handle(player, instruction)
                } catch (e: Throwable) {
                    logger.error(e) { "Error in instruction $instruction" }
                }
            }
        }
    }

    companion object {
        const val MAX_INSTRUCTIONS = 20
    }
}