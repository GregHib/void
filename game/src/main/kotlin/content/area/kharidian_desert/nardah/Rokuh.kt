package content.area.kharidian_desert.nardah

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class Rokuh : Script {
    init {
        npcOperate("Talk-to", "rokuh*") { (target) ->
            npc<Happy>("Come one, come all, buy my amazing choc ice invention here! Chocolate on the outside. Iced cream on the inside. Oh I also have some chocolate left over from making choc ices which I'm selling too.")
            choice {
                option("Cool I'd like to buy some.")
                option<Neutral>("No thanks, I'm not interested.")
                option("How do you stop your icy snacks melting?") {
                    player<Quiz>("How do you stop your icy snacks melting? The middle of the desert is the last place I'd expect to see ice.")
                    npc<Happy>("It's quite a surprise, isn't it, my friend. I actually have this special magic box of ice, which I bought for a princely sum, from a strange man while adventuring in lands far away. He said it is imbued with some sort of")
                    npc<Happy>("powerful ice magic which keeps it cold all the time. I had never seen any ice magic before, it's clearly very rare and powerful, so I felt I had to buy it.")
                }
                if (questCompleted("spirits_of_the_elid")) {
                    option<Quiz>("How's business, now that the curse has gone?") {
                        npc<Happy>("Ohh as good as ever. The people of Nardah seem to have taken well to the taste of chocolate and iced cream combined and, with the curse gone, all my friends are much happier too, so it's all good.")
                    }
                }
            }
        }
    }
}
