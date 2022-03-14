package world.gregs.voidps

import world.gregs.voidps.engine.client.instruction.InstructionTask
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.CharacterUpdateTask
import world.gregs.voidps.engine.client.update.MovementTask
import world.gregs.voidps.engine.client.update.PathTask
import world.gregs.voidps.engine.client.update.batch.ChunkBatches
import world.gregs.voidps.engine.client.update.iterator.ParallelIterator
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.update.npc.NPCResetTask
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerResetTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.engine.tick.Scheduler
import world.gregs.voidps.network.NetworkQueue
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.VisualEncoder
import world.gregs.voidps.network.visual.VisualMask.NPC_FORCE_CHAT_MASK
import world.gregs.voidps.network.visual.VisualMask.NPC_WATCH_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_FORCE_CHAT_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_WATCH_MASK
import world.gregs.voidps.network.visual.Visuals
import world.gregs.voidps.network.visual.encode.ForceChatEncoder
import world.gregs.voidps.network.visual.encode.WatchEncoder
import world.gregs.voidps.network.visual.encode.npc.*
import world.gregs.voidps.network.visual.encode.player.*

fun getTickStages(
    players: Players,
    npcs: NPCs,
    items: FloorItems,
    objects: Objects,
    queue: NetworkQueue,
    batches: ChunkBatches,
    pathFinder: PathFinder,
    collisions: Collisions,
    scheduler: Scheduler,
    objectDefinitions: ObjectDefinitions,
    npcDefinitions: NPCDefinitions,
    interfaceDefinitions: InterfaceDefinitions,
    handler: InterfaceHandler,
    sequentialNpc: TaskIterator<NPC> = SequentialIterator(),
    sequentialPlayer: TaskIterator<Player> = SequentialIterator(),
    parallelPlayer: TaskIterator<Player> = ParallelIterator(),
    parallelNpc: TaskIterator<NPC> = ParallelIterator()
) = listOf(
    PlayerResetTask(sequentialPlayer, players, batches),
    NPCResetTask(sequentialNpc, npcs),
    // Connections/Tick Input
    queue,
    // Tick
    InstructionTask(players, npcs, items, objects, collisions, objectDefinitions, npcDefinitions, interfaceDefinitions, handler),
    scheduler,
    PathTask(parallelPlayer, players, pathFinder),
    MovementTask(sequentialPlayer, players, collisions),
    PathTask(parallelNpc, npcs, pathFinder),
    MovementTask(sequentialNpc, npcs, collisions),
    // Update
    CharacterUpdateTask(
        parallelPlayer,
        players,
        PlayerUpdateTask(players, playerVisualEncoders()),
        NPCUpdateTask(npcs, npcVisualEncoders()),
        batches
    ),
    AiTick()
)

private class AiTick : Runnable {
    override fun run() {
        World.events.emit(AiTick)
    }
}

private fun playerVisualEncoders() = castOf<PlayerVisuals>(
    WatchEncoder(PLAYER_WATCH_MASK),
    PlayerTimeBarEncoder(),
    ForceChatEncoder(PLAYER_FORCE_CHAT_MASK),
    PlayerHitsEncoder(),
    FaceEncoder(),
    PlayerForceMovementEncoder(),
    PlayerSecondaryGraphicEncoder(),
    PlayerColourOverlayEncoder(),
    TemporaryMoveTypeEncoder(),
    PlayerPrimaryGraphicEncoder(),
    PlayerAnimationEncoder(),
    AppearanceEncoder(),
    MovementTypeEncoder()
)

private fun npcVisualEncoders() = castOf<NPCVisuals>(
    TransformEncoder(),
    NPCAnimationEncoder(),
    NPCPrimaryGraphicEncoder(),
    TurnEncoder(),
    NPCForceMovementEncoder(),
    NPCColourOverlayEncoder(),
    NPCHitsEncoder(),
    WatchEncoder(NPC_WATCH_MASK),
    ForceChatEncoder(NPC_FORCE_CHAT_MASK),
    NPCTimeBarEncoder(),
    NPCSecondaryGraphicEncoder()
)

@Suppress("UNCHECKED_CAST")
private fun <T : Visuals> castOf(vararg encoders: VisualEncoder<out Visuals>) = encoders
    .map { it as VisualEncoder<T> }