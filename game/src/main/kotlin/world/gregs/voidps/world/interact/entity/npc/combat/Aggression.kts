package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPCModes
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayerModes
import world.gregs.voidps.engine.entity.character.player.PlayerOption

huntPlayerModes("aggressive", "cowardly") { npc ->
   npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntNPCModes("aggressive", "cowardly") { npc ->
   npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}