package content.minigame.pest_control

import content.entity.player.dialogue.Cry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Mariah : Script {
    init {
        // https://runescape.wiki/w/Transcript:Mariah#After_Quiet_Before_the_Swarm
        npcOperate("Talk-to", "mariah_upset_pest_control") {
            npc<Cry>("He's dead! He's dead! My Mikhal is dead!")
            player<Neutral>("Maybe you should go back to the mainland, see some family?")
            npc<Sad>("And leave his grave? Never! I'm going to stay here, with him.")
        }
    }
}
