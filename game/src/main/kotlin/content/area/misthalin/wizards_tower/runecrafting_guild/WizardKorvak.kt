package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class WizardKorvak : Script {
    init {
        npcOperate("Talk-to", "wizard_korvak") {
            npc<Scared>("AAAAAAAAAAH!")
            npc<Angry>("Don't sneak up on me like that.")
            player<Confused>("Uh, sorry.")
            choice {
                option("Why are you so jumpy?") {
                    player<Quiz>("Why are you so jumpy?")
                    npc<Angry>("I am not jumpy! I am insane. There is a difference.")
                    player<Confused>("If you are insane, wouldn't you think you were sane and so be insensible to the insanity that is you?")
                    npc<Shifty>("Yebno.")
                    player<Confused>("Beg your pardon?")
                    npc<Happy>("Yebno. It's a fast way of saying Yes Maybe No. Since I didn't know the answer to your question I gave you all of the answers.")
                    player<Quiz>("How did you end up in your rather peculiar mindset?")
                    npc<Scared>("They sent me to the place. They knew the dark wizards were there and someone had betrayed them. Us. So they sent me to spy. To the place.")
                    npc<Sad>("They sent me and would not let me come back.")
                    player<Shifty>("Who sent you? What betrayal?")
                    npc<Sad>("He sits there with the spinning light, always thinking. One of us led them to the place where the pickaxe hammers and so the betrayal happened.")
                    npc<Scared>("Please don't make me talk about it.")
                    player<Sad>("Okay, we don't have to talk about it.")
                }
                option("Who is that wizard outside the guild portal?") {
                    npc<Happy>("Ah, the young Finix: brilliant young wizard!")
                    npc<Angry>("wizard_vief", "Young troublemaker, more like!")
                    npc<Bored>("Just because his theories clash with yours, it doesn't make them incorrect.")
                    npc<Angry>("wizard_vief", "The sheer notion of the altars running out is preposterous! You cannot run out of space in an infinitely folded dimension.")
                    npc<Frustrated>("Just because the space is infinite, it doesn't mean the energy inside fills all the space.")
                    npc<Angry>("wizard_vief", "Of course it does! We all know that the energy inside a dimension must be evenly spread to stop it collapsing in on itself.")
                    npc<Frustrated>("That may be true, but why are the altars in Gielinor cracked, then?")
                    npc<Confused>("wizard_vief", "Er...I'm going to leave you guys to it.")
                }
                option("Never mind.")
            }
        }
    }
}
