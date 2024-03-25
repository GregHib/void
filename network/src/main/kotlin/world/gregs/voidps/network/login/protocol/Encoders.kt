package world.gregs.voidps.network.login.protocol

import world.gregs.voidps.network.login.protocol.visual.*
import world.gregs.voidps.network.login.protocol.visual.encode.ForceChatEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.WatchEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.npc.*
import world.gregs.voidps.network.login.protocol.visual.encode.player.*

fun playerVisualEncoders() = castOf<PlayerVisuals>(
    WatchEncoder(VisualMask.PLAYER_WATCH_MASK),
    PlayerTimeBarEncoder(),
    ForceChatEncoder(VisualMask.PLAYER_FORCE_CHAT_MASK),
    PlayerHitsEncoder(),
    PlayerTurnEncoder(),
    PlayerExactMovementEncoder(),
    PlayerSecondaryGraphicEncoder(),
    PlayerColourOverlayEncoder(),
    TemporaryMoveTypeEncoder(),
    PlayerPrimaryGraphicEncoder(),
    PlayerAnimationEncoder(),
    AppearanceEncoder(),
    MovementTypeEncoder()
)

fun npcVisualEncoders() = castOf<NPCVisuals>(
    TransformEncoder(),
    NPCAnimationEncoder(),
    NPCPrimaryGraphicEncoder(),
    NPCTurnEncoder(),
    NPCExactMovementEncoder(),
    NPCColourOverlayEncoder(),
    NPCHitsEncoder(),
    WatchEncoder(VisualMask.NPC_WATCH_MASK),
    ForceChatEncoder(VisualMask.NPC_FORCE_CHAT_MASK),
    NPCTimeBarEncoder(),
    NPCSecondaryGraphicEncoder()
)

@Suppress("UNCHECKED_CAST")
private fun <T : Visuals> castOf(vararg encoders: VisualEncoder<out Visuals>) = encoders
    .map { it as VisualEncoder<T> }