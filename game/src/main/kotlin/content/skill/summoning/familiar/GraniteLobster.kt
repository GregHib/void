package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class GraniteLobster : Script {
    init {
        npcOperate("Interact", "granite_lobster_familiar") {
            if (questCompleted("fremennik_trials")) {
                when (random.nextInt(2)) {
                    0 -> {
                        npc<Neutral>("Ho, my Fremennik brother, shall we go raiding?")
                        player<Happy>("Well, I suppose we could when I'm done with this.")
                        npc<Neutral>("Yes! To the looting and the plunder!")
                    }
                    1 -> {
                        npc<Neutral>("We shall heap the helmets of the fallen into a mountain!")
                        player<Happy>("The outerlanders have insulted our heritage for the last time!")
                        npc<Neutral>("The longhall will resound with our celebration!")
                    }
                }
                return@npcOperate
            }
            npc<Neutral>("Clonkclonk clonk grind clonk. (Keep walking, outerlander. We have nothing to discuss.)")
            player<Happy>("Fair enough.")
            npc<Neutral>("Clonkclonkclonk grind clonk grind? (It's nothing personal, you're just an outerlander, you know?)")
        }
    }
}
