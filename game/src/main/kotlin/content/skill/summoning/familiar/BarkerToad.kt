package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.random

class BarkerToad : Script {
    init {
        npcOperate("Interact", "barker_toad_familiar") {
            if (inventory.contains("swamp_toad")) {
                npc<Neutral>("Bwaaarp graaaawk? (What's that croaking in your inventory?)")
                player<Happy>("Ah, you mean that toad?")
                player<Happy>("Oh, I'm guessing you're not going to like me carrying a toad about.")
                npc<Neutral>("Craaawk, croak. (I might not be all that happy, no.)")
                player<Happy>("I'm not going to eat it.")
                npc<Neutral>("Craaaaawk braaap croak. (Weeeeell, I'd hope not! Reminds me of my mama toad. She was inflated and fed to a jubbly, you know. A sad, demeaning way to die.)")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Ladies and gentlemen, for my next trick, I shall swallow this fly!")
                    player<Happy>("Seen it.")
                    npc<Neutral>("Ah, but last time was the frog...on fire?")
                    player<Happy>("No! That would be a good trick.")
                    npc<Neutral>("Well, it won't be this time either.")
                    player<Happy>("Awwwww...")
                }
                1 -> {
                    npc<Neutral>("Roll up, roll up, roll up! See the greatest show on Gielinor!")
                    player<Happy>("Where?")
                    npc<Neutral>("Well, it's kind of...you.")
                    player<Happy>("Me?")
                    npc<Neutral>("Roll up, roll up, roll up! See the greatest freakshow on Gielinor!")
                    player<Happy>("Don't make me smack you, slimy.")
                }
                2 -> {
                    npc<Neutral>("We need to set up the big top somewhere near here. The locals look friendly enough.")
                    player<Happy>("Are you kidding?")
                    npc<Neutral>("Your problem is that you never see opportunities.")
                }
                3 -> {
                    npc<Neutral>("Braaaaaaaaaaaaaaaaaaaaaaap! (*Burp!*)")
                    player<Happy>("That's disgusting behaviour!")
                    npc<Neutral>("Braap craaaaawk craaaawk. (That, my dear boy, was my world-renowned belching.)")
                    player<Happy>("I got that part. Why are you so happy about it?")
                    npc<Neutral>("Braaaaaaap craaaaaawk craaaaaaaawk. (My displays have bedazzled the crowned heads of Gielinor.)")
                    player<Happy>("I'd give you a standing ovation, but I have my hands full.")
                }
            }
        }
    }
}
