package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.Uncertain
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class Hans {

    init {
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
    }
}
