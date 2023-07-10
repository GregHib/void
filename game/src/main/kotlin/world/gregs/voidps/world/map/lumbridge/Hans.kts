package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.EvilLaugh
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ operate && npc.id == "hans" && option == "Talk-to" }) { player: Player ->
    npc<Talk>("Hello. What are you doing here?")
    choice {
        option<Talk>("I'm looking for whoever is in charge of this place.") {
            npc<Talk>("Who, the Duke? He's in his study, on the first floor.")
        }
        option<EvilLaugh>("I have come to kill everyone in this castle!") {
            npc.forceChat = "Help! Help!"
            npc.mode = Retreat(npc, player)
        }
        option<Uncertain>("I don't know. I'm lost. Where am I?") {
            npc<Talk>("You are in Lumbridge Castle.")
        }
    }
}