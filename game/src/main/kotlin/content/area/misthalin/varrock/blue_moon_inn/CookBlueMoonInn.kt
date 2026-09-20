package content.area.misthalin.varrock.blue_moon_inn

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Horrified
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

// Source: https://runescape.wiki/w/Transcript:Cook_(Blue_Moon_Inn)?oldid=27997081
class CookBlueMoonInn : Script {
    init {
        npcOperate("Talk-to", "cook_blue_moon_inn") {
            npc<Angry>("What do you want? I'm busy!")
            choice {
                option<Quiz>("Can you sell me any food?") {
                    npc<Neutral>("I suppose I could sell you some cabbage, if you're willing to pay for it. Cabbage is good for you.")
                    choice{
                        option<Neutral>("Alright, I'll buy a cabbage. ") {
                            TODO("ID for shop hasn't been found")
                        }
                        option<Neutral>("No thanks, I don't like cabbage.") {
                            npc<Angry>("Bah! People these days only appreciate junk food.")
                        }
                    }
                }
                option<Quiz>("Can you give me any free food?") {
                    npc<Quiz>("Can you give me any free money?")
                    player<Confused>("Why should I give you free money?")
                    npc<Quiz>("Why should I give you free food?")
                    player<Neutral>("Oh, forget it.")
                }
                option<Neutral>("I don't want anything from this horrible kitchen.") {
                    npc<Angry>("How dare you? I put a lot of effort into cleaning this kitchen. My daily sweat and elbow-grease keep this kitchen clean!")
                    player<Horrified>("Ewww!")
                    npc<Neutral>("Oh, just leave me alone.")
                }
            }
        }
    }
}