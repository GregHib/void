package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Talking
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && target.id == "captain_daerkin" && option == "Talk-to" }) { player: Player ->
    npc<Cheerful>("Hello old chap.")
    player<Talking>("What are you doing here? Shouldn't you be looking after your glider?")
    npc<Cheerful>("I'm pretty much retired these days old fellow. My test piloting days are over. I'm just relaxing here and enjoying the primal clash between man and man.")
    player<Talking>("You're watching the duels then. Are you going to challenge someone yourself?")
    npc<Cheerful>("I do find the duels entertaining to watch, but I suspect that actually being involved would be a lot less fun for me. I'm a lover, not a fighter!")
    player<Talking>("Errm, I suppose you are.")
}
