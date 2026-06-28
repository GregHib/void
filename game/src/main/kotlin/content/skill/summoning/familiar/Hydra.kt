package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Hydra : Script {
    init {
        npcOperate("Interact", "hydra_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Raaaspraaasp? (Isn't it hard to get things done with just one head?)")
                    player<Happy>("Not really!")
                    npc<Neutral>("Raaasp raaaaap raaaasp? (Well I suppose you work with what you got, right?")
                    npc<Neutral>("Raaaaaasp raaaasp raaaasp. (At least he doesn't have someone whittering in their ear all the time.)")
                    npc<Neutral>("Raaaaaaasp! (Quiet, you!)")
                }
                1 -> {
                    npc<Neutral>("Raaaasp raaaasp! (Man, I feel good!)")
                    npc<Neutral>("Raaasp ssssss raaaasp. (That's easy for you to say.)")
                    player<Happy>("What's up?")
                    npc<Neutral>("Raaa.... (well...)")
                    npc<Neutral>("Raaaaasp sss rassssp. (Don't pay any attention, they are just feeling whiny.)")
                    player<Happy>("But they're you, aren't they?")
                    npc<Neutral>("Raaaasp raasp rasssp! (Don't remind me!)")
                }
                2 -> {
                    npc<Neutral>("Rassssp rasssssp! (You know, two heads are better than one!)")
                    npc<Neutral>("Raaaasp rassssp sssssp.... (Unless you're the one doing all the heavy thinking....)")
                    player<Happy>("I think I'll stick to one for now, thanks.")
                }
                3 -> {
                    npc<Neutral>("Raaaaaaasp. (Siiiigh.)")
                    npc<Neutral>("Raasp raasp raaaaasp? (What's up this time?)")
                    player<Happy>("Can I help?")
                    npc<Neutral>("Rasssp ssssssp? raaaaasp raaaasp. (Do you mind? This is a private conversation.)")
                    player<Happy>("Well, excu-u-use me.")
                }
            }
        }
    }
}
