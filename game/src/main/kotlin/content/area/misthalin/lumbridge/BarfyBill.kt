package content.area.misthalin.lumbridge

import content.entity.obj.canoe.minimumCanoeLevel
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BarfyBill : Script {

    init {
        npcOperate("Talk-To", "barfy_bill") {
            player<Idle>("Hello there.")
            npc<Idle>("Oh! Hello there.")
            choice {
                option<Idle>("Who are you?") {
                    npc<Idle>("My name is Ex Sea Captain Barfy Bill.")
                    player<Quiz>("Ex sea captain?")
                    npc<Disheartened>("Yeah, I bought a lovely ship and was planning to make a fortune running her as a merchant vessel.")
                    player<Quiz>("Why are you not still sailing?")
                    npc<Disheartened>("Chronic sea sickness. My first, and only, voyage was spent dry heaving over the rails.")
                    npc<Idle>("If I had known about the sea sickness I could have saved myself a lot of money.")
                    player<Confused>("What are you up to now then?")
                    npc<Shifty>("Well my ship had a little fire related problem. Fortunately it was well insured.")
                    npc<Idle>("Anyway, I don't have to work anymore so I've taken to canoeing on the river.")
                    npc<Happy>("I don't get river sick!")
                    npc<Quiz>("Would you like to know how to make a canoe?")
                    choice {
                        option("Yes") {
                            canoeing()
                        }
                        option("No") {
                            player<Idle>("No thanks, not right now.")
                        }
                    }
                }
                option("Can you teach me about Canoeing?") {
                    canoeing()
                }
            }
        }
    }

    suspend fun Player.canoeing() {
        if (minimumCanoeLevel()) {
            return
        }
        npc<Idle>("It's really quite simple to make. Just walk down to that tree on the bank and chop it down.")
        npc<Idle>("When you have done that you can shape the log further with your axe to make a canoe.")
        when (levels.get(Skill.Woodcutting)) {
            in 12..26 -> {
                npc<Idle>("Hah! I can tell just by looking that you lack talent in woodcutting.")
                player<Quiz>("What do you mean?")
                npc<Happy>("No Callouses! No Splinters! No camp fires littering the trail behind you.")
                npc<Happy>("Anyway, the only 'canoe' you can make is a log. You'll be able to travel 1 stop along the river with a log canoe.")
            }
            in 27..41 -> {
                npc<Happy>("With your skill in woodcutting you could make my favourite canoe, the Dugout. They might not be the best canoe on the river, but they get you where you're going.")
                player<Quiz>("How far will I be able to go in a Dugout canoe?")
                npc<Happy>("You will be able to travel 2 stops on the river.")
            }
            in 42..56 -> {
                npc<Happy>("The best canoe you can make is a Stable Dugout, one step beyond a normal Dugout.")
                npc<Happy>("With a Stable Dugout you can travel to any place on the river.")
                player<Quiz>("Even into the Wilderness?")
                npc<Happy>("Not likely! I've heard tell of a man up near Edgeville who claims he can use a Waka to get up into the Wilderness.")
                npc<Quiz>("I can't think why anyone would wish to venture into that hellish landscape though.")
            }
            else -> {
                npc<Happy>("Hoo! You look like you know which end of an axe is which!")
                npc<Idle>("You can easily build one of those Wakas. Be careful if you travel into the Wilderness though.")
                npc<Scared>("I've heard tell of great evil in that blasted wasteland.")
                player<Idle>("Thanks for the warning Bill.")
            }
        }
    }
}
