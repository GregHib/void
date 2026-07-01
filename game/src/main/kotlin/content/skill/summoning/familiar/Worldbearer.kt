package content.skill.summoning.familiar

import content.entity.player.dialogue.Frustrated
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Worldbearer : Script {
    init {
        npcOperate("Interact", "*_worldbearer_familiar") {
            if (!this["talked_to_worldbearer", false]) {
                this["talked_to_worldbearer"] = true
                npc<Frustrated>("I am not a great talker, little cub. For my sake, make it quick.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("Do you need help carrying?")
                    npc<Frustrated>("Little cub, there is no greater insult to a worldbearer. Shall I rub your belly so you can digest? Shall I move your feet so you can walk?")
                    npc<Frustrated>("Garra! I know your culture: you call us pack mules and servants. There is no indignity in what I do! There is only honour in bearing another's burdens.")
                    player<Happy>("I didn't mean to insult you.")
                    npc<Frustrated>("Bah, you are right. I must have woken on the wrong side of the stream today, little cub. Ignore me.")
                    player<Happy>("You're carrying a few burdens of your own.")
                    npc<Frustrated>("Grrrr! Although my role is to carry - and I carry for you now - I like it as much as I like your stale odour. Can we press on before I do something that I will get exiled for?")
                }
                1 -> {
                    player<Happy>("You shouldn't complain so much.")
                    npc<Frustrated>("Do not pretend that we are in the servant and master role, little cub! Our alliance is a delicate one and will end some day, through good means or bad.")
                    npc<Frustrated>("When that day comes, you may not find that I am so helpful. Our goal is to blast this dungeon apart, but I have no issue with leaving you behind.")
                }
                2 -> {
                    player<Happy>("What is it like to be a worldbearer?")
                    npc<Frustrated>("The worldbearers are the legs and back of the gorajo; we bear the provisions, tents and tools from one destination to another.")
                    npc<Frustrated>("And when the gorajo are not moving, the worldbearers prepare and serve the food. It is an important role, and one that is esteemed among my clansmen.")
                    player<Happy>("It sounds like hard work. There can't be much time to enjoy yourself.")
                    npc<Frustrated>("It is rewarding in its own way. When a worldbearer is put to rest, they are stripped of all belongings, to be reincarnated as a creature without burden: a sparrowhawk or a wildcat, perhaps.")
                    npc<Frustrated>("Although we face trials in this life, our next is free and joyful.")
                    player<Happy>("You carry everything and have to serve it too?")
                    npc<Frustrated>("The clan sees the worldbearer as the nurturing mother wolf: proudly defending her pack while carrying the food and delivering it to her pups.")
                    npc<Frustrated>("I would prefer a more masculine comparison; never mind, it has been so for centuries.")
                }
                3 -> {
                    player<Happy>("I don't have any more questions.")
                    npc<Frustrated>("That is good to hear. I am not one to talk.")
                }
            }
        }
    }
}
