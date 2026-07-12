package content.area.asgarnia.taverley

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script

class IvySophista : Script {

    init {
        // Taken from RS3
        npcOperate("Talk-to", "ivy_sophista") {
            choice {
                option("Ask about the chest.") {
                    npc<Neutral>("That chest? Oh, it's simply here for safekeeping. We keep it here as a reward for determined treasure-seekers who find both key halves and assemble them into the crystal key which can open the chest!")
                }
                option("Ask about herself.") {
                    player<Happy>("What do you do here?")
                    npc<Neutral>("Ah, well, I'm keeping an eye on things. Making sure nothing unexpected happens.")
                    player<Neutral>("That's reassuring.")
                    npc<Neutral>("One day I hope it will be.")
                }
            }
        }
    }
}
