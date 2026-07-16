package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class WarTortoise : Script {
    init {
        npcOperate("Interact", "war_tortoise_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    statement("*The tortoise waggles its head about*")
                    npc<Neutral>("What are we doing in this dump?")
                    player<Happy>("Well, I was just going to take care of a few things.")
                    statement("*The tortoise shakes its head*")
                    npc<Neutral>("I don't believe it. Stuck here with this young whippersnapper running around having fun.")
                    player<Happy>("You know, I'm sure you would enjoy it if you gave it a chance.")
                    npc<Neutral>("Oh, you would say that, wouldn't you?")
                }
                1 -> {
                    npc<Neutral>("Hold up a minute, there.")
                    player<Happy>("What do you want?")
                    statement("*The tortoise bobs its head*")
                    npc<Neutral>("For you to slow down!")
                    player<Happy>("Well, I've stopped now.")
                    npc<Neutral>("Yes, but you'll soon start up again, won't you?")
                    player<Happy>("Probably.")
                    statement("* The tortoise waggles its head despondently.*")
                    npc<Neutral>(" I don't believe it....")
                }
                2 -> {
                    statement("* The tortoise bobs its head around energetically.*")
                    npc<Neutral>("Oh, so now you're paying attention to me, are you?")
                    player<Happy>("I pay you plenty of attention!")
                    npc<Neutral>("Only when you want me to carry those heavy things of yours.")
                    player<Happy>("I don't ask you to carry anything heavy.")
                    npc<Neutral>("What about those lead ingots?")
                    player<Happy>("What lead ingots?")
                    statement("*The tortoise droops its head*")
                    npc<Neutral>("Well, that's what it felt like....")
                    npc<Neutral>("*grumble grumble*")
                }
                3 -> {
                    statement("*The tortoise exudes an air of reproach*")
                    npc<Neutral>("Are you going to keep rushing around all day?")
                    player<Happy>("Only for as long as I have the energy to.")
                    npc<Neutral>("Oh. I'm glad that my not being able to keep up with you brings you such great amusement.")
                    player<Happy>("I didn't mean it like that.")
                    statement("*The tortoise waggles its head disapprovingly.*")
                    npc<Neutral>("Well, when you are QUITE finished laughing at my expense, how about you pick up a rock larger than your body and go crawling about with it?")
                    npc<Neutral>("We'll see how energetic you are after an hour or two of that.")
                }
            }
        }
    }
}
