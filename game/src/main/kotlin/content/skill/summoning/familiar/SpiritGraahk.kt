package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SpiritGraahk : Script {
    init {
        npcOperate("Interact", "spirit_graahk_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("Your spikes are looking particularly spiky today.")
                    npc<Neutral>("Really, you think so?")
                    player<Happy>("Yes. Most pointy, indeed.")
                    npc<Neutral>("That's really kind of you to say. I was going to spike you but I won't now...")
                    player<Happy>("Thanks?")
                    npc<Neutral>("...I'll do it later instead.")
                    player<Happy>("*sigh!*")
                }
                1 -> {
                    npc<Neutral>("My spikes hurt, could you pet them for me?")
                    player<Happy>("Aww, of course I can I'll just... Oww! I think you drew blood that time.")
                }
                2 -> {
                    npc<Neutral>("Hi!")
                    player<Happy>("Hello. Are you going to spike me again?")
                    npc<Neutral>("No, I got a present to apologise for last time.")
                    player<Happy>("That's really sweet, thank you.")
                    npc<Neutral>("Here you go, it's a special cushion to make you comfortable.")
                    player<Happy>("It's made of spikes!")
                    npc<Neutral>("Yes, but they're therapeutic spikes.")
                    player<Happy>("...")
                }
                3 -> {
                    player<Happy>("How's your day going?")
                    npc<Neutral>("It's great! Actually I've got something to show you!")
                    player<Happy>("Oh? What's that?")
                    npc<Neutral>("You'll need to get closer!")
                    player<Happy>("I can't see anything...")
                    npc<Neutral>("It's really small - even closer.")
                    player<Happy>("Oww! I'm going to have your spikes trimmed!")
                }
            }
        }
    }
}
