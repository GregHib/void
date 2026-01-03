package world.gregs.voidps.network.login.protocol

import world.gregs.voidps.network.login.protocol.visual.*
import world.gregs.voidps.network.login.protocol.visual.encode.SayEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.WatchEncoder
import world.gregs.voidps.network.login.protocol.visual.encode.npc.*

fun npcVisualEncoders() = castOf<NPCVisuals>(
    TransformEncoder(),
    NPCAnimationEncoder(),
    NPCPrimaryGraphicEncoder(),
    NPCFaceEncoder(),
    NPCExactMovementEncoder(),
    NPCColourOverlayEncoder(),
    NPCHitsEncoder(),
    WatchEncoder(VisualMask.NPC_WATCH_MASK),
    SayEncoder(VisualMask.NPC_SAY_MASK),
    NPCTimeBarEncoder(),
    NPCSecondaryGraphicEncoder(),
)

@Suppress("UNCHECKED_CAST")
private fun <T : Visuals> castOf(vararg encoders: VisualEncoder<out Visuals>) = encoders
    .map { it as VisualEncoder<T> }.toTypedArray()
