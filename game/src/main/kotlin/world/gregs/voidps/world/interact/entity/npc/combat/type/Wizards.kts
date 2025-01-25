package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.world.interact.entity.combat.npcCombatPrepare
import content.skill.magic.spell.spell

npcCombatPrepare { npc ->
    npc.spell = npc.def.getOrNull<String>("spell") ?: return@npcCombatPrepare
}