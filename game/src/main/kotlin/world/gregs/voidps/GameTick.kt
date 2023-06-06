package world.gregs.voidps

import world.gregs.voidps.engine.client.instruction.InstructionTask
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.update.CharacterUpdateTask
import world.gregs.voidps.engine.client.update.NPCTask
import world.gregs.voidps.engine.client.update.PlayerTask
import world.gregs.voidps.engine.client.update.batch.ChunkBatchUpdates
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.client.update.npc.NPCResetTask
import world.gregs.voidps.engine.client.update.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.player.PlayerResetTask
import world.gregs.voidps.engine.client.update.player.PlayerUpdateTask
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.NPCDefinitions
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.AiTick
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItemTracking
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
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
    floorItems: FloorItemTracking,
    objects: GameObjects,
    queue: NetworkQueue,
    factory: PlayerFactory,
    batches: ChunkBatchUpdates,
    objectDefinitions: ObjectDefinitions,
    npcDefinitions: NPCDefinitions,
    interfaceDefinitions: InterfaceDefinitions,
    handler: InterfaceHandler,
    parallelPlayer: TaskIterator<Player>
): List<Runnable> {
    val sequentialNpc: TaskIterator<NPC> = SequentialIterator()
    val sequentialPlayer: TaskIterator<Player> = SequentialIterator()
    return listOf(
        PlayerResetTask(sequentialPlayer, players, batches),
        NPCResetTask(sequentialNpc, npcs),
        // Connections/Tick Input
        queue,
        factory,
        // Tick
        InstructionTask(players, npcs, items, objects, objectDefinitions, npcDefinitions, interfaceDefinitions, handler),
        World,
        NPCTask(sequentialNpc, npcs),
        PlayerTask(sequentialPlayer, players),
        floorItems,
        objects.timers,
        // Update
        batches,
        CharacterUpdateTask(
            parallelPlayer,
            players,
            PlayerUpdateTask(players, playerVisualEncoders()),
            NPCUpdateTask(npcs, npcVisualEncoders()),
            batches
        ),
        AiTick()
    )
}

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
    PlayerTurnEncoder(),
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
    NPCTurnEncoder(),
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