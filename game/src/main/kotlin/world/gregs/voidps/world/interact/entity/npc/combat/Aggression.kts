package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption

huntPlayer(mode = "aggressive") { npc ->
   npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntPlayer(mode = "cowardly") { npc ->
   npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntNPC(mode = "aggressive") { npc ->
   npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

huntNPC(mode = "cowardly") { npc ->
   npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}