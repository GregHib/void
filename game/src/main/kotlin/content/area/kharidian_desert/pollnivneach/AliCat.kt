package content.area.kharidian_desert.pollnivneach

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.pet.hasCatspeakAmulet
import world.gregs.voidps.engine.Script

class AliCat : Script {

    init {
        // https://runescape.wiki/w/Transcript:Ali_Cat
        npcOperate("Talk-to", "ali_cat_pollnivneach_2") {
            if (!hasCatspeakAmulet()) {
                npc<Neutral>("Meeeeow.")
                player<Neutral>("Ah yes, I can't understand you without my amulet of catspeak.")
                return@npcOperate
            }
            npc<Happy>("So, you've finished Sumona's mission and you're now using her to your advantage. Well done.")
            player<Neutral>("Well, I wouldn't put it quite like that.")
            npc<Happy>("Oh, but I would. Using folk who think they're superior, but you know are not, is a useful life skill. You're becoming ever more feline, which is a good thing - trust me.")
            player<Happy>("I think that was a compliment, so thanks.")
        }
    }
}
