package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.world.interact.entity.combat.npcCombatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

npcCombatPrepare { npc ->
    val spell = npc.def.getOrNull<String>("spell") ?: return@npcCombatPrepare
    npc.spell = spell
}