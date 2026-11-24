package content.quest.member.fairy_tale_part_2.hideout

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class HideoutFairy : Script {
    init {
        // https://oldschool.runescape.wiki/w/Transcript:Fairy_(Fairy_Resistance_Hideout)#After_Fairytale_II_-_Cure_a_Queen
        npcOperate("Talk-to", "fairy_fairy_hq,fairy_fairy_hq_2_2,fairy_fairy_hq_3_2,fairy_fairy_hq_4_2,fairy_fairy_hq_5_2") {
            when (random.nextInt(6)) {
                0 -> npc<Neutral>("The Queen is the true ruler of Zanaris! We shall overthrow the tyrant!")
                1 -> npc<Quiz>("Is the council finished yet?")
                2 -> npc<Neutral>("Once the council of war is finished we shall go and get rid of the Godfather.")
                3 -> npc<Happy>("I'm so looking forward to going back home!")
                4 -> npc<Happy>("Thank you for waking our queen.")
                else -> npc<Happy>("Now that the Queen is awake everything will be alright.")
            }
        }
    }
}
