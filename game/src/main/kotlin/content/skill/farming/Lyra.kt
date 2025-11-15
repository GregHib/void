package content.skill.farming

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Lyra : Script {
    init {
        npcOperate("Talk-to", "lyra") {
            choice {
                option<Quiz>("Would you look after my crops for me?") {
                    npc<Talk>("I might - which patch were you thinking of?")
                    choice {
                        option("The northwestern allotment") {
                            // TODO patch check
                            npc<Talk>("If you like, but I want two buckets of compost for that.")
                        }
                        option("The southeastern allotment") {
                            // TODO patch check
                            npc<Talk>("You don't have any seeds planted in that patch. Plant some and I might agree to look after it for you.")
                        }
                    }
                }
                option<Quiz>("Can you give me any farming advice?") {
                    npc<Talk>("Hops are good for brewing ales. I believe there's a brewery up in Keldagrim somewhere, and I've heard rumours that a place called Phasmatys used to be good for that type of thing. 'Fore they all died, of course.")
                }
//                option<Quiz>("Can you sell me something?") {
//                    npc<Talk>("That depends on whether I have it to sell. What is it that you're looking for?")
//                }
                option<Talk>("I'll come back another time.")
            }
        }
    }
}
