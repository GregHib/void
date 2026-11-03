package content.quest.member.fairy_tale_part_2.hideout

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class HideoutFairyVeryWise : Script {
    init {
        // https://runescape.wiki/w/Transcript:A_Fairy_Tale_II_-_Cure_a_Queen#Talking_to_Fairy_Very_Wise_after_waking_up_the_queen
        npcOperate("Talk-to", "fairy_very_wise_fairy_hq_2") {
            npc<Happy>("Thank you for your great service to the fairy people, we are indebted to you!")
        }
    }
}
