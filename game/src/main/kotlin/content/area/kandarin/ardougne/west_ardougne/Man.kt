package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script

@Script
class Man {

    init {
        npcOperate("Talk-to", "w_ardougnecitizen3") {
            player<Happy>("Good day.")
            npc<Surprised>("An outsider! Can you get me out of this hell hole?")
            player<Sad>("Sorry, that's not what I'm here to do.")
        }

        npcOperate("Talk-to", "w_ardougnecitizen4") {
            player<Happy>("Hello there.")
            npc<Angry>("Go away. People from the outside shut us in like animals. I have nothing to say to you.")
        }
    }
}
