package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun run() {
        for (player in players) {
            val instructions = player.instructions
            for (instruction in instructions.replayCache) {
                if (player["debug", false]) {
                    logger.debug { "${player.accountName} ${player.tile} - $instruction" }
                }
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