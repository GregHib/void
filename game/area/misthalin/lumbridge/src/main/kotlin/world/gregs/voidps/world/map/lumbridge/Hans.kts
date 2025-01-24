package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.EvilLaugh
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.Uncertain
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

npcOperate("Talk-to", "hans") {
    npc<Talk>("Hello. What are you doing here?")
    choice {
        option<Talk>("I'm looking for whoever is in charge of this place.") {
            npc<Talk>("Who, the Duke? He's in his study, on the first floor.")
        }
        option<EvilLaugh>("I have come to kill everyone in this castle!") {
            target.say("Help! Help!")
            target.mode = Retreat(target, player)
        }
        option<Uncertain>("I don't know. I'm lost. Where am I?") {
            npc<Talk>("You are in Lumbridge Castle.")
        }
    }
}