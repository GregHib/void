package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Deathslinger : Script {
    init {
        npcOperate("Interact", "*_deathslinger_familiar") {
            if (!this["talked_to_deathslinger", false]) {
                this["talked_to_deathslinger"] = true
                npc<Frustrated>("We have much work to do, but I could stop for a moment.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("What is the biggest creature you have killed?")
                    npc<Frustrated>("Haha, you sound like my cubs! What did you kill, mama? Did it have ten heads, mama? Did it fire magic bolts from its eyes?")
                    player<Happy>("Alright, I get it. I was just making conversation.")
                    npc<Frustrated>("Do not sulk, naabe. You simply reminded me of better times. To answer your question, it was most likely a sinkhole. They are huge and flat, shaped something like an open palm. ")
                    npc<Frustrated>("They burrow beneath the ground, and then fold themselves into a fist, storing the land and people within them to be digested when required.")
                    player<Happy>("That's horrible!")
                    npc<Frustrated>("And so much worse to be inside one. This place is nothing in comparison to a sinkhole, naabe. I relish every day outside of that thing.")
                }
                1 -> {
                    player<Happy>("How much do you know about Daemonheim?")
                    npc<Frustrated>("I know that it has been here for far longer than you or I have been alive, beyond the lifetimes of our parents, grandparents and any relatives they knew.")
                    npc<Frustrated>("Many, from so many different races, have been born here. And many have died here, filling the holes they helped to dig. It is not a life they deserved, but they knew no other.")
                    player<Happy>("It must have been a terrible life.")
                    npc<Frustrated>("It is best to not consider it a life, naabe. They would have burrowed without question, knowing no life better than this. Like blind moles, churra. ")
                    npc<Frustrated>("They believed that they were burrowing to an exit. It is hateful to think that their leader may have played upon this fact, encouraging them downward to their escape.")
                }
                2 -> {
                    player<Happy>("Why do the gorajo have only one role? You can't be a deathslinger all the time, can you?")
                    npc<Frustrated>("A gorajo needs but one role. How do you humans say it? We...specialise.")
                    player<Happy>("I guess that would make you a pure. I mean, adventurers who specialise in one skill are often called pures.")
                    npc<Frustrated>("A pure? I like this. The goraju are pure of action, pure of purpose... Yes, I will accept this term.")
                }
                3 -> {
                    player<Happy>("I don't have any more questions.")
                    npc<Frustrated>("Fly fast on the wind, young naabe.")
                }
            }
        }
    }
}
