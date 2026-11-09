package content.quest.member.fairy_tale_part_2.hideout

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name

class HideoutFairyNuff : Script {
    init {
        // https://runescape.wiki/w/Transcript:A_Fairy_Tale_II_-_Cure_a_Queen#Post-quest_dialogue_(prior_to_Fairy_Tale_III)
        npcOperate("Talk-to", "fairy_nuff_fairy_hq_2") {
            npc<Happy>("Thank you so much for all you've done for us, $name.")
        }
    }
}
