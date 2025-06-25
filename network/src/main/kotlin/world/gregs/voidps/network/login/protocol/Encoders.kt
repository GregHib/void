package world.gregs.voidps.network.login.protocol

import world.gregs.voidps.network.login.protocol.visual.*
import world.gregs.voidps.network.login.protocol.visual.encode.SayEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.npc.NPCWatchEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.npc.*
import world.gregs.voidps.network.login.protocol.visual.encode.player.*

fun playerVisualEncoders() = castOf<PlayerVisuals>(
    // Animate Worn
    PlayerAnimationEncoder(),
    PlayerThirdGraphicEncoder(),
    PlayerColourOverlayEncoder(),
    TemporaryMoveTypeEncoder(),
    PlayerTimeBarEncoder(),
    PlayerFourthGraphicEncoder(),
    // Clanmate
    PlayerHitsEncoder(),
    // Worn
    AppearanceEncoder(),
    SayEncoder(VisualMask.PLAYER_SAY_MASK),
    // PICON
    MovementTypeEncoder(),
    PlayerWatchEncoder(),
    PlayerExactMovementEncoder(),
    PlayerFaceEncoder(),
    PlayerPrimaryGraphicEncoder(),
    PlayerSecondaryGraphicEncoder()
)

fun npcVisualEncoders() = castOf<NPCVisuals>(
    NPCThirdGraphicEncoder(),
    NPCWatchEncoder(),
    NPCFourthGraphicEncoder(),
    NPCHitsEncoder(),
    NPCTimeBarEncoder(),
    NPCNameEncoder(),
    TransformEncoder(),
    SayEncoder(VisualMask.NPC_SAY_MASK),
    NPCFaceEncoder(),
    NPCCombatLevelEncoder(),
    NPCWornEncoder(),
    NPCCustomiseEncoder(),
    NPCExactMovementEncoder(),
    NPCAnimationEncoder(),
    NPCCustomiseEncoder(),
    NPCAnimateWornEncoder(),
    NPCSecondaryGraphicEncoder(),
    NPCPrimaryGraphicEncoder(),
    NPCColourOverlayEncoder(),
)

@Suppress("UNCHECKED_CAST")
private fun <T : Visuals> castOf(vararg encoders: VisualEncoder<out Visuals>) = encoders
    .map { it as VisualEncoder<T> }