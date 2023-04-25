package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects

class InstructionTask(
    private val players: Players,
    npcs: NPCs,
    items: FloorItems,
    objects: Objects,
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
        objectDefinitions,
        npcDefinitions,
        interfaceDefinitions,
        handler
    )

    override fun run() {
        players.forEach { player ->
            val instructions = player.instructions
            for (instruction in instructions.replayCache) {
                logger.debug { "${player.accountName} ${player.tile} - $instruction" }
                try {
                    handlers.handle(player, instruction)
                } catch (e: Throwable) {
                    logger.error(e) { "Error in instruction $instruction" }
                }
            }
            instructions.resetReplayCache()
        }
    }
}