package world.gregs.voidps.engine.entity.character.update.visual.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.network.visual.VisualMask.TRANSFORM_MASK

fun NPC.flagTransform() = visuals.flag(TRANSFORM_MASK)