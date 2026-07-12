package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class ThornySnail : Script {
    init {
        npcOperate("Interact", "thorny_snail_familiar") {
            if (equipped(EquipSlot.Hat).id.contains("snelm")) {
                npc<Neutral>("...")
                player<Happy>("What's the matter?")
                npc<Neutral>("Check your head...")
                player<Happy>("What about it... Oh, wait! Oh, this is pretty awkward...")
                npc<Neutral>("You're wearing the spine of one of my relatives as a hat...")
                player<Happy>("Well more of a faux-pas, then.")
                npc<Neutral>("Just a bit...")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("All this running around the place is fun!")
                    player<Happy>("I'll be it's a step up from your usually sedentary lifestyle!")
                    npc<Neutral>("True, but it's mostly seeing the sort of sights you don't get at home.")
                    player<Happy>("Such as?")
                    npc<Neutral>("Living things for a start.")
                    player<Happy>("Those are in short supply in Mort Myre, I admit.")
                }
                1 -> {
                    npc<Neutral>("I think my stomach is drying out...")
                    player<Happy>("Your stomach? How do you know how it's feeling?")
                    npc<Neutral>("I am walking on it, you know...")
                    player<Shifty>("Urrgh...")
                }
                2 -> {
                    npc<Neutral>("Okay, I have to ask, what are those things you people totter about on?")
                    player<Happy>("You mean my legs?")
                    npc<Neutral>("Yes, those. How are you supposed to eat anything through them?")
                    player<Happy>("Well, we don't. That's what our mouths are for.")
                    npc<Neutral>("Oh, right! I thought those were for expelling waste gas and hot air!")
                    player<Happy>("Well, for a lot of people they are.")
                }
                3 -> {
                    player<Happy>("Can you slow down?")
                    npc<Neutral>("Are we going too fast for you?")
                    player<Happy>("I bet if you had to run on your internal organs you'd want a break now and then!")
                }
            }
        }
    }
}
