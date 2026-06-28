package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SpiritDagannoth : Script {
    init {
        npcOperate("Interact", "spirit_dagannoth_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Grooooooowl graaaaawl raaaawl? (Are you ready to surrender to the power of the Deep Waters?)")
                    player<Happy>("Err, not really.")
                    npc<Neutral>("Rooooowl? (How about now?)")
                    player<Happy>("No, sorry.")
                    npc<Neutral>("Rooooowl? (How about now?)")
                    player<Happy>("No, sorry. You might want to try again a little later.")
                }
                1 -> {
                    npc<Neutral>("Groooooowl. Hsssssssssssssss! (The Deeps will swallow the lands. None will stand before us!)")
                    player<Happy>("What if we build boats?")
                    npc<Neutral>("Hsssssssss groooooowl? Hssssshsss grrooooooowl? (What are boats? The tasty wooden containers full of meat?)")
                    player<Happy>("I suppose they could be described as such, yes.")
                }
                2 -> {
                    npc<Neutral>("Hssssss graaaawl grooooowl, growwwwwwwwwl! (Oh how the bleak gulfs hunger for the Day of Rising.)")
                    player<Happy>("My brain hurts when I listen to you talk...")
                    npc<Neutral>("Raaaaawl groooowl grrrrawl! (That's the truth biting into your clouded mind!)")
                    player<Happy>("Could you try using a little less truth please?")
                }
                3 -> {
                    npc<Neutral>("Raaaawl! (Submit!)")
                    player<Happy>("Submit to what?")
                    npc<Neutral>("Hssssssssss rawwwwwl graaaawl! (To the inevitable defeat of all life on the Surface!)")
                    player<Happy>("I think I'll wait a little longer before I just keep over and submit, thanks")
                    npc<Neutral>("Hsssss, grooooowl, raaaaawl. (Well, it's your choice, but those that submit first will be eaten first.)")
                    player<Happy>("I'll pass on that one, thanks.")
                }
            }
        }
    }
}
