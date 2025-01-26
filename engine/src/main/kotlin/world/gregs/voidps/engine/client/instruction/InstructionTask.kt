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

/**
 * Represents a task responsible for handling instructions for players.
 * This class processes and executes a maximum number of instructions per player
 * during each run cycle, utilizing defined instruction handlers.
 *
 * @property players The collection of players to process instructions for.
 * @property npcs The collection of non-player characters available in the game world.
 * @property items The collection of floor items available in the game world.
 * @property objects The collection of interactive game objects within the game world.
 * @property itemDefinitions The definitions and configurations for various item types.
 * @property objectDefinitions The definitions and configurations for various game object types.
 * @property npcDefinitions The definitions and configurations for available NPC types.
 * @property interfaceDefinitions The definitions related to graphical interfaces used in the game.
 * @property handler An interface handler responsible for managing interface interactions.
 */
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

    /**
     * Logger used for recording debug and error messages related to the processing
     * of player instructions in the `InstructionTask` class. Provides inline logging
     * capabilities for better performance and structured output.
     */
    private val logger = InlineLogger()
    /**
     * A collection of instruction handlers utilized to process various game-related entities and definitions.
     * This includes processing for players, NPCs, floor items, game objects, and interface-related interactions.
     *
     * @property players Reference to manage and process player-related instructions.
     * @property npcs Reference to handle NPC-related instructions.
     * @property items Reference to manage floor item-related instructions.
     * @property objects Reference to process game object-related instructions.
     * @property itemDefinitions Definitions required for item-related handling.
     * @property objectDefinitions Definitions required for game object-related instructions.
     * @property npcDefinitions Definitions used for NPC-related processes.
     * @property interfaceDefinitions Definitions related to interface interaction handling.
     * @property handler Responsible for managing the interface interactions.
     */
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

    /**
     * Executes the main processing logic for handling player instructions.
     *
     * This method iterates over all players in the game and processes a set number of instructions
     * (defined by `MAX_INSTRUCTIONS`) for each player. Instructions are received from the player's
     * instruction queue. If a player has debug mode enabled (retrieved via `player["debug", false]`),
     * a debug log is generated for each instruction processed. Any errors encountered during the
     * handling of an instruction are logged.
     *
     * Each instruction is passed to the `handlers.handle` method for further processing,
     * which delegates the instruction to the appropriate handler based on its type.
     *
     * If no instruction is found in the player's queue, processing breaks early for that player.
     */
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

    /**
     * Companion object for the InstructionTask class.
     * Provides static constants or utility functions specific to the class.
     */
    companion object {
        /**
         * The maximum number of instructions that can be processed for a player in a single run cycle.
         *
         * This constant is used to limit the number of instructions a player can execute to avoid
         * excessive processing and ensure fair turn-based execution among all active players.
         */
        const val MAX_INSTRUCTIONS = 20
    }
}