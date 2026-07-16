package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class StrangerPlant : Script {
    init {
        npcOperate("Interact", "stranger_plant_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("I'M STRANGER PLANT!")
                    player<Happy>("I know you are.")
                    npc<Neutral>("I KNOW! I'M JUST SAYING!")
                    player<Happy>("Do you have to shout like that all of the time?")
                    npc<Neutral>("WHO'S SHOUTING?")
                    player<Happy>("If this is you speaking normally, I'd hate to hear you shouting.")
                    npc<Neutral>("OH, SNAP!")
                }
                1 -> {
                    npc<Neutral>("WILL WE HAVE TO BE HERE LONG?")
                    player<Happy>("We'll be here until I am finished.")
                    npc<Neutral>("BUT THERE'S NO DRAMA HERE!")
                    player<Happy>("Well, how about you pretend to be an undercover agent.")
                    npc<Neutral>("WONDERFUL! WHAT'S MY MOTIVATION?")
                    player<Happy>("You're trying to remain stealthy and secretive, while looking out for clues.")
                    npc<Neutral>("I'LL JUST GET INTO CHARACTER! AHEM!")
                    npc<Neutral>("PAPER! PAPER! VARROCK HERALD FOR SALE!")
                    player<Happy>("What kind of spy yells loudly like that?")
                    npc<Neutral>("ONE WHOSE COVER IDENTITY IS A PAPER-SELLER, OF COURSE!")
                    player<Happy>("Ask a silly question...")
                }
                2 -> {
                    npc<Neutral>("DIIIIVE!")
                    player<Happy>("What? Help! Why dive?")
                    npc<Neutral>("OH, DON'T WORRY! I JUST LIKE TO YELL THAT FROM TIME TO TIME!")
                    player<Happy>("Well, can you give me a little warning next time?")
                    npc<Neutral>("WHAT, AND TAKE ALL THE FUN OUT OF LIFE?")
                    player<Happy>("If by 'fun' you mean 'sudden heart attacks', then yes, please take them out of my life!")
                }
                3 -> {
                    npc<Neutral>("I THINK I'M WILTING!")
                    player<Happy>("Do you need some water?")
                    npc<Neutral>("DON'T BE SILLY! I CAN PULL THAT OUT OF THE GROUND!")
                    player<Happy>("Then why are you wilting?")
                    npc<Neutral>("IT'S SIMPLE: THERE'S A DISTINCT LACK OF DRAMA!")
                    player<Happy>("Drama?")
                    npc<Neutral>("YES, DRAMA!")
                    player<Happy>("Okay...")
                    player<Happy>("Let's see if we can find some for you.")
                    npc<Neutral>("LEAD ON!")
                }
            }
        }
    }
}
