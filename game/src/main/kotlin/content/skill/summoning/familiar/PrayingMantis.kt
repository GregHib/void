package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class PrayingMantis : Script {
    init {
        npcOperate("Interact", "praying_mantis_familiar") {
            if (inventory.contains("butterfly_net") || equipment.contains("butterfly_net")) {
                npc<Neutral>("Clatter click chitter click? (Wouldn't you learn focus better if you used chopsticks?)")
                player<Happy>("Huh?")
                npc<Neutral>("Clicker chirrpchirrup. (For catching the butterflies, grasshopper.)")
                player<Happy>("Oh, right! Well, if I use anything but the net I squash them.")
                npc<Neutral>("Chirrupchirrup click! (Then, I could have them!)")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Chitter chirrup chirrup? (Have you been following your training, grasshopper?)")
                    player<Happy>("Yes, almost every day.")
                    npc<Neutral>("Chirrupchirrup chirrup. ('Almost' is not good enough.)")
                    player<Happy>("Well, I'm trying as hard as I can.")
                    npc<Neutral>("Chirrup chitter chitter chirrup? (How do you expect to achieve enlightenment at this rate, grasshopper?)")
                    player<Happy>("Spontaneously.")
                }
                1 -> {
                    npc<Neutral>("Chitterchitter chirrup clatter. (Today, grasshopper, I will teach you to walk on rice paper.)")
                    player<Happy>("What if I can't find any?")
                    npc<Neutral>("Clatter chitter click chitter... (Then we will wander about and punch monsters in the head...)")
                    player<Happy>("I could do in an enlightened way if you want?")
                    npc<Neutral>("Chirrupchitter! (That will do!)")
                }
                2 -> {
                    npc<Neutral>("Clatter chirrup chirp chirrup clatter clatter. (A wise man once said; 'Feed your mantis and it will be happy'.)")
                    player<Happy>("Is there any point to that saying?")
                    npc<Neutral>("Clatter chirrupchirrup chirp. (I find that a happy mantis is its own point.)")
                }
                3 -> {
                    npc<Neutral>("Clatter chirrupchirp- (Today, grasshopper, we will-)")
                    player<Happy>("You know, I'd rather you call me something other than grasshopper.")
                    npc<Neutral>("Clitterchirp? (Is there a reason for this?)")
                    player<Happy>("You drool when you say it.")
                    npc<Neutral>("Clickclatter! Chirrup chirpchirp click chitter... (I do not! Why would I drool when I cann you a juicy...)")
                    npc<Neutral>("...clickclick chitter clickchitter click... (...succulent, nourishing, crunchy...)")
                    npc<Neutral>("*Drooool*")
                    player<Happy>("You're doing it again!")
                }
            }
        }
    }
}
