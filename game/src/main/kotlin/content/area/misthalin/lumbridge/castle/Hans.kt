package content.area.misthalin.lumbridge.castle

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.EvilLaugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Retreat

class Hans : Script {

    init {
        npcOperate("Talk-to", "hans") { (target) ->
            npc<Neutral>("Hello. What are you doing here?")
            choice {
                option<Neutral>("I'm looking for whoever is in charge of this place.") {
                    npc<Neutral>("Who, the Duke? He's in his study, on the first floor.")
                }
                option<EvilLaugh>("I have come to kill everyone in this castle!") {
                    target.say("Help! Help!")
                    target.mode = Retreat(target, this)
                }
                option<Confused>("I don't know. I'm lost. Where am I?") {
                    npc<Neutral>("You are in Lumbridge Castle.")
                }
            }
        }
    }
}
