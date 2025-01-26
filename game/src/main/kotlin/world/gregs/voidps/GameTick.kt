package world.gregs.voidps

import world.gregs.voidps.engine.client.instruction.InstructionTask
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.CharacterUpdateTask
import world.gregs.voidps.engine.client.update.NPCTask
import world.gregs.voidps.engine.client.update.PlayerTask
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.update.npc.NPCResetTask
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerResetTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.data.SaveQueue
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.AiTick
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.hunt.Hunting
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItemTracking
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.protocol.npcVisualEncoders
import world.gregs.voidps.network.login.protocol.playerVisualEncoders

/**
 * Generates a list of tasks for processing game logic, including player and NPC resets,
 * connections, updates, and other queued activities. The tasks generated are executed in the game's tick cycle.
 *
 * @param players The collection of players in the game. Default is obtained from the dependency injection framework.
 * @param npcs The collection of NPCs in the game. Default is obtained from the dependency injection framework.
 * @param items The collection of floor items in the game. Default is obtained from the dependency injection framework.
 * @param floorItems The tracking system for floor items. Default is obtained from the dependency injection framework.
 * @param objects The collection of game objects. Default is obtained from the dependency injection framework.
 * @param queue The connection queue handling players' actions. Default is obtained from the dependency injection framework.
 * @param accountSave The queue responsible for saving account states. Default is obtained from the dependency injection framework.
 * @param batches Handles updates to game zones. Default is obtained from the dependency injection framework.
 * @param itemDefinitions The definitions for all item types in the game. Default is obtained from the dependency injection framework.
 * @param objectDefinitions The definitions for all object types in the game. Default is obtained from the dependency injection framework.
 * @param npcDefinitions The definitions for all NPC types in the game. Default is obtained from the dependency injection framework.
 * @param interfaceDefinitions Game interface definitions for UI handling. Default is obtained from the dependency injection framework.
 * @param hunting The hunting task that handles hunting-related mechanics. Default is obtained from the dependency injection framework.
 * @param handler The interface handler used to manage game interactions. Default is instantiated with dependencies from the framework.
 * @param sequential A flag to determine whether the task execution should be sequential (true) or parallel (false). Default is the debugging mode value.
 * @return A list of Runnable tasks to be executed in the game tick cycle.
 */
fun getTickStages(
    players: Players = get(),
    npcs: NPCs = get(),
    items: FloorItems = get(),
    floorItems: FloorItemTracking = get(),
    objects: GameObjects = get(),
    queue: ConnectionQueue = get(),
    accountSave: SaveQueue = get(),
    batches: ZoneBatchUpdates = get(),
    itemDefinitions: ItemDefinitions = get(),
    objectDefinitions: ObjectDefinitions = get(),
    npcDefinitions: NPCDefinitions = get(),
    interfaceDefinitions: InterfaceDefinitions = get(),
    hunting: Hunting = get(),
    handler: InterfaceHandler = InterfaceHandler(get(), get(), get()),
    sequential: Boolean = CharacterTask.DEBUG
): List<Runnable> {
    val sequentialNpc: TaskIterator<NPC> = SequentialIterator()
    val sequentialPlayer: TaskIterator<Player> = SequentialIterator()
    val iterator: TaskIterator<Player> = if (sequential) SequentialIterator() else ParallelIterator()
    return listOf(
        PlayerResetTask(sequentialPlayer, players, batches),
        NPCResetTask(sequentialNpc, npcs),
        hunting,
        // Connections/Tick Input
        queue,
        // Tick
        InstructionTask(players, npcs, items, objects, itemDefinitions, objectDefinitions, npcDefinitions, interfaceDefinitions, handler),
        World,
        NPCTask(sequentialNpc, npcs),
        PlayerTask(sequentialPlayer, players),
        floorItems,
        objects.timers,
        // Update
        batches,
        CharacterUpdateTask(
            iterator,
            players,
            PlayerUpdateTask(players, playerVisualEncoders()),
            NPCUpdateTask(npcs, npcVisualEncoders()),
            batches
        ),
        AiTick(),
        accountSave
    )
}

/**
 * Represents a task that emits the `AiTick` event to the world when executed.
 * This class implements the `Runnable` interface and is designed to be run in a thread or scheduled task,
 * triggering the `AiTick` event for handling AI-related updates in the game world.
 */
private class AiTick : Runnable {
    override fun run() {
        World.emit(AiTick)
    }
}