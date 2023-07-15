package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.HuntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.on

on<HuntPlayer>({ mode == "aggressive" || mode == "cowardly" }) { npc: NPC ->
   npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

on<HuntNPC>({ mode == "aggressive" || mode == "cowardly" }) { npc: NPC ->
   npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}