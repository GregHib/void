package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class AliMorrisane : Script {

    init {
        npcOperate("Talk-to", "ali_morrisane") {
            npc<Neutral>("Hello, my friend. Have a look at my wares.")
            choice {
                option<Neutral>("No, I'm really too busy.")
                option("Okay.") {
                    openShop("alis_discount_wares")
                }
            }
        }
    }

    fun ChoiceOption.whatsInIt() {
        option("So what's in this for me?") {
            player<Neutral>("What's in this for me?")
            npc<Confused>("I thought you wanted to help.")
            player<Neutral>("Of course, but everyone seems to be benefiting here but me.")
            npc<Neutral>("What do you mean? ")
            player<Neutral>("Well the trader gets their product sold, you get your cut, but what do I get?")
            npc<Neutral>("You get access to more products. Is that not reward enough?")
            npc<Neutral>("Hmmm... Perhaps not.")
            npc<Neutral>("I'll tell you what then, how about I let you have discounted flights on my magic carpet rides?")
            set("roguetrader_var", 2080) // https://chisel.weirdgloop.org/varbs/display?varplayer=540
            player<Neutral>("Wow that would be great.")
            npc<Neutral>("Fair enough so, we have a win, win, win situation on our hands then.")
            player<Neutral>("Is there anyone else you want me to talk to on your behalf?")
            npc<Neutral>("There's a blackjack seller in Pollnivneach, a cautious type of guy, not the usual entrepreneurial sort that I would normally deal with, but...")
            npc<Neutral>("I think that he has a product with real potential...")
            player<Neutral>("What? .... Blackjacks?")
            npc<Neutral>("Yes, but at the moment the product isn't polished enough, if you could convince him to create a larger variety of weapon I would gladly stock his products.")
            set("roguetrader_var", 2084) // https://chisel.weirdgloop.org/varbs/display?varplayer=540
            choice {
                whatsInIt()
                okayIllSee()
            }
        }
    }

    fun ChoiceOption.okayIllSee() {
        option<Neutral>("Ok I'll see what I can do.") {
            npc<Neutral>("I think I need just one more product type, something magical ... now what could I stock that's magical, has large demand and a pretty good profit margin...")
            player<Neutral>("How about runes?")
            npc<Sad>("Yes, that would be perfect, but who could I get that would supply? There really seems to be a gap in the market in the desert but I don't know anyone in that line of business.")
            player<Happy>("Hang on, I know somebody called Aubury, decent fella. I helped him out a while back. Perhaps I could approach him about the idea.")
            npc<Happy>("Oh yes, the rune seller from Varrock. That would be perfect. He's far enough away that he wouldn't end up competing with himself.")
            set("roguetrader_var", 2340) // https://chisel.weirdgloop.org/varbs/display?varplayer=540
            player<Quiz>("What do you mean?")
            npc<Neutral>("Well the rune market is a limited one, if I were to sell his runes from a location nearby his shop, then I would take part of his market share so he would in fact not benefit from me selling runes.")
            player<Happy>("Ah ha I think I know where this is going. So because you're located far enough away from him you will take up someone else's share in the market and Aubury will make more money as a result.")
            npc<Neutral>("Very well anticipated.")
            choice {
                whatsInIt()
                option<Neutral>("Ok I'll see what I can do.")
            }
        }
    }

    suspend fun Player.fued() {
        player<Neutral>("Hi Ali. Not bad. How are you?")
        npc<Neutral>("Still selling.")
        npc<Neutral>("Hello $name. How's the adventuring going?")
        player<Neutral>("Hi Ali. Not bad. How are you?")
        npc<Neutral>("Still selling.")
        choice {
            if (questCompleted("the_feud")) {
                option("I would like to have a look at your selection of blackjacks.")
            }
            option("I would like to have a look at your selection of clothes.")
            option("I would like to have a look at your general stock.")
        }
        player<Neutral>("Anything interesting?")
        npc<Happy>("For you, always. As a matter of fact I have just acquired a fleet of magic carpets, which operate throughout the Kharidian desert.")
        player<Neutral>("Wow, that sounds great, how about your stall?")
        npc<Neutral>("Trade is good, but I would like to expand and diversify, but it's proving a little more difficult than I initially thought.")
        player<Confused>("How so?")
        npc<Shifty>("I think the other merchants in this town are conspiring against me.")
        player<Neutral>("Oh come on! You're just being paranoid now.")
        npc<Neutral>("Well maybe I can't get any new suppliers then because I'm Pollnivnian.")
        player<Confused>("You're what?")
        npc<Bored>("From Pollnivneach.")
        player<Neutral>("Maybe I could help you then?")
        npc<Neutral>("Perhaps you could.")
        if (questCompleted("icthlarins_little_helper")) {
            npc<Neutral>("I have an old friend - a cloth merchant Siamun, who lives in Sophanem - Do you know the place?")
            player<Neutral>(" Umm..... Images of cats and priests come to mind. Ah yes! There we go, it's the city of the dead South of Pollnivneach isn't it?")
            npc<Neutral>("That's the one, well if you could convince him to supply me with some of his merchandise, I would be more than willing to sell them to the masses ...... for a small percentage of course.")
            choice {
                whatsInIt()
                option("Ok I'll get on to it.")
            }
        } else {
            // TODO
        }
    }
}
