package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class RavenousLocust : Script {
    init {
        npcOperate("Interact", "ravenous_locust_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Click whiiine whiiiiine click click? (Hey, man, can you spare some lentils?)")
                    player<Happy>("What would you want with lentils?")
                    npc<Neutral>("Whiiiiiinewhiiiiiiine click whiiiiiiiine. (I was going to make a casserole.)")
                    player<Happy>("How? You don't have a fire, pans or thumbs.")
                    npc<Neutral>("Whiiiiiiiiiiiiine! (Stop hassling me, man.)")
                }
                1 -> {
                    npc<Neutral>("Whiiiiiine click click! (Man, it's a totally groovy day.)")
                    player<Happy>("That it is.")
                    npc<Neutral>("Whiiiiine whiiiiine whinewhiiiine. (Now, if only I wasn't being held down by 'The Man'.)")
                    player<Happy>("Which man?")
                    npc<Neutral>("Clickclack whiiiiiine whiiiiinewhiiine. ('The Man'; the one that keeps harshing my mellow.)")
                    player<Happy>("'Harshing your mellow'? Okay, I don't want to know any more.")
                }
                2 -> {
                    npc<Neutral>("Whiiiiiine... (Siiiiigh...)")
                    player<Happy>("What's the matter?")
                    npc<Neutral>("Whiiiine whiiiineclickwhiiiiine whine... (I was just thinking about how meat is murder...)")
                    player<Happy>("But it isn't. Killing someone is murder.")
                    npc<Neutral>("Click click! (Good point.)")
                }
                3 -> {
                    npc<Neutral>("Whiiiiine whinewhiiiine? (Man, how about time?)")
                    player<Happy>("I think it's about midday.")
                    npc<Neutral>("Clickwhiiiiine whiiiiiiiiiiiiine... (No, man. Isn't time, like, massive?)")
                    player<Happy>("I don't think an abstract concept can have mass...")
                    npc<Neutral>("Whineclick click! (Oh, man, that's heavy.)")
                }
            }
        }
    }
}
