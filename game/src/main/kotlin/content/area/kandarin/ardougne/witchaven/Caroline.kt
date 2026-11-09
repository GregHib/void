package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Caroline : Script {

    init {
        // https://runescape.wiki/w/Transcript:Caroline (After Kennith's Concerns)
        npcOperate("Talk-to", "caroline_witchaven_5") {
            player<Neutral>("Hello again, Caroline. Are you all rested after your ordeal?")
            npc<Happy>("Yes, I'm feeling much better, thank you. Kennith is doing well out on the platform too.")
            player<Happy>("I'm glad to hear it. I hope he's behaving himself!")
            npc<Neutral>("Oh yes, Bailey has him at work trying to clear the platform of the sea slugs. He's even started writing letters to Kimberly!")
            player<Neutral>("Really? How has she reacted to that?")
            npc<Neutral>("I don't think she's quite ready to forgive him yet, but I don't think Kennith is ready to give up either. To tell you the truth, I think he's smitten with her! Who'd have thought it!")
            player<Neutral>("Yes, that's quite a revelation. Well, I hope it works out for him. I might go out to the Fishing Platform to visit him sometime soon.")
            npc<Happy>("Oh, I think he'd like that - he's always talking about you!")
            player<Neutral>("I'll do my best.")
        }
    }
}
