package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Meerkats : Script {
    init {
        npcOperate("Interact", "meerkats_familiar") {
            npc<Neutral>("Chatter Chatter. Chatter chatter chatter chatter chatter. Chatter! (We're pretty unlucky. Often, we hit a box when we try to burrow where you tell us. Very suspicious!)")
            player<Happy>("Well, if we remove all the boxes, you'll be able to burrow anywhere!")
            npc<Neutral>("Chatter chatter chatter! (Then the boxes must be removed!)")
            npc<Neutral>("Chatter! (Agreed!)")
            npc<Neutral>("Chatter chatter chatter! (Let's dig out those boxes!)")
            player<Happy>("That's the spirit!")
        }
    }
}
