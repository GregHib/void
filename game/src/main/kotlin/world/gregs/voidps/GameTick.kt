package world.gregs.voidps

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.instruction.InstructionTask
import world.gregs.voidps.engine.client.update.encode.ForceChatEncoder
import world.gregs.voidps.engine.client.update.encode.WatchEncoder
import world.gregs.voidps.engine.client.update.encode.npc.*
import world.gregs.voidps.engine.client.update.encode.player.*
import world.gregs.voidps.engine.client.update.task.MovementTask
import world.gregs.voidps.engine.client.update.task.PathTask
import world.gregs.voidps.engine.client.update.task.npc.NPCChangeTask
import world.gregs.voidps.engine.client.update.task.npc.NPCPostUpdateTask
import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.client.update.task.npc.NPCVisualsTask
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask
import world.gregs.voidps.engine.client.update.task.player.PlayerPostUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerUpdateTask
import world.gregs.voidps.engine.client.update.task.player.PlayerVisualsTask
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.visual.NPC_FORCE_CHAT_MASK
import world.gregs.voidps.engine.entity.character.update.visual.NPC_WATCH_MASK
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_FORCE_CHAT_MASK
import world.gregs.voidps.engine.entity.character.update.visual.PLAYER_WATCH_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.TRANSFORM_MASK
import world.gregs.voidps.engine.entity.character.update.visual.npc.TURN_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.APPEARANCE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.FACE_DIRECTION_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.MOVEMENT_TYPE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.TEMPORARY_MOVE_TYPE_MASK
import world.gregs.voidps.engine.map.chunk.ChunkBatches
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.AiTick
import world.gregs.voidps.engine.tick.Tick
import world.gregs.voidps.network.NetworkQueue

fun getTickStages(
    players: Players,
    npcs: NPCs,
    queue: NetworkQueue,
    batches: ChunkBatches,
    scheduler: Scheduler,
    pathFinder: PathFinder,
    collisions: Collisions
) = listOf(
    // Connections/Tick Input
    queue,
    // Tick
    InstructionTask(players),
    GameTick(),
    PathTask(players, pathFinder),
    PathTask(npcs, pathFinder),
    MovementTask(players, collisions),
    MovementTask(npcs, collisions),
    // Update
    batches,
    ViewportUpdating(),
    PlayerVisualsTask(players, playerVisualEncoders(), defaultPlayerVisuals()),
    NPCVisualsTask(npcs, npcVisualEncoders(), defaultNpcVisuals()),
    PlayerChangeTask(players),
    NPCChangeTask(npcs),
    PlayerUpdateTask(players),
    NPCUpdateTask(players),
    PlayerPostUpdateTask(players),
    NPCPostUpdateTask(npcs),
    AiTick()
)

private class GameTick : Runnable {
    override fun run() {
        GameLoop.inFlow = true
        GameLoop.flow.tryEmit(GameLoop.tick)
        GameLoop.inFlow = false
        World.events.emit(Tick(GameLoop.tick))
    }
}

private class AiTick: Runnable {
    override fun run() {
        World.events.emit(AiTick)
    }
}

private fun playerVisualEncoders() = castOf(
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

private fun defaultPlayerVisuals() = intArrayOf(
    FACE_DIRECTION_MASK,
    TEMPORARY_MOVE_TYPE_MASK,
    APPEARANCE_MASK,
    MOVEMENT_TYPE_MASK,
)

private fun npcVisualEncoders() = castOf(
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

private fun defaultNpcVisuals() = intArrayOf(
    TRANSFORM_MASK,
    TURN_MASK
)

@Suppress("UNCHECKED_CAST")
private fun castOf(vararg encoders: VisualEncoder<out Visual>) = encoders
    .map { it as VisualEncoder<Visual> }
    .toTypedArray()