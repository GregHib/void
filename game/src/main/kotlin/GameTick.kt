import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InstructionTask
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

fun getTickStages(
    players: Players = get(),
    npcs: NPCs = get(),
    items: FloorItems = get(),
    floorItems: FloorItemTracking = get(),
    objects: GameObjects = get(),
    queue: ConnectionQueue = get(),
    accountSave: SaveQueue = get(),
    batches: ZoneBatchUpdates = get(),
    hunting: Hunting = get(),
    sequential: Boolean = CharacterTask.DEBUG,
    handlers: InstructionHandlers = get()
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
        npcs,
        items,
        // Tick
        InstructionTask(players, handlers),
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

private class AiTick : Runnable {
    override fun run() {
        World.emit(AiTick)
    }
}