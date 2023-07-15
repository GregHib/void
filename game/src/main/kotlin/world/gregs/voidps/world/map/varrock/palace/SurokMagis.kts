package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Angry
import world.gregs.voidps.world.interact.dialogue.Surprised
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && target.id == "surok_magis" && option == "Talk-to" }) { player: Player ->
    npc<Angry>("Can't you see I'm very busy here? Be off with you!")
    player<Surprised>("Oh. Sorry.")
}