package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.network.login.protocol.visual.VisualMask

fun NPC.flagTransform() = visuals.flag(VisualMask.TRANSFORM_MASK)

fun NPC.flagCombatLevel() = visuals.flag(VisualMask.NPC_COMBAT_LEVEL)

fun NPC.flagName() = visuals.flag(VisualMask.NPC_NAME)