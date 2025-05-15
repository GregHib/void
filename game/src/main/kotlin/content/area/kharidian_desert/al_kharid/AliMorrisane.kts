package content.area.kharidian_desert.al_kharid

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceBuilder
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

npcOperate("Talk-to", "ali_morrisane") {
    npc<Talk>("Hello, my friend. Have a look at my wares.")
    choice {
        option<Talk>("No, I'm really too busy.")
        option("Okay.") {
            player.openShop("alis_discount_wares")
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.whatsInIt() {
    option("So what's in this for me?") {
        player<Talk>("What's in this for me?")
        npc<Uncertain>("I thought you wanted to help.")
        player<Talk>("Of course, but everyone seems to be benefiting here but me.")
        npc<Talk>("What do you mean? ")
        player<Talk>("Well the trader gets their product sold, you get your cut, but what do I get?")
        npc<Talk>("You get access to more products. Is that not reward enough?")
        npc<Talk>("Hmmm... Perhaps not.")
        npc<Talk>("I'll tell you what then, how about I let you have discounted flights on my magic carpet rides?")
        player["roguetrader_var"] = 2080 // https://chisel.weirdgloop.org/varbs/display?varplayer=540
        player<Talk>("Wow that would be great.")
        npc<Talk>("Fair enough so, we have a win, win, win situation on our hands then.")
        player<Talk>("Is there anyone else you want me to talk to on your behalf?")
        npc<Talk>("There's a blackjack seller in Pollnivneach, a cautious type of guy, not the usual entrepreneurial sort that I would normally deal with, but...")
        npc<Talk>("I think that he has a product with real potential...")
        player<Talk>("What? .... Blackjacks?")
        npc<Talk>("Yes, but at the moment the product isn't polished enough, if you could convince him to create a larger variety of weapon I would gladly stock his products.")
        player["roguetrader_var"] = 2084 // https://chisel.weirdgloop.org/varbs/display?varplayer=540
        choice {
            whatsInIt()
            okayIllSee()
        }
    }
}

fun ChoiceBuilder<NPCOption<Player>>.okayIllSee() {
    option<Talk>("Ok I'll see what I can do.") {
        npc<Talk>("I think I need just one more product type, something magical ... now what could I stock that's magical, has large demand and a pretty good profit margin...")
        player<Talk>("How about runes?")
        npc<Upset>("Yes, that would be perfect, but who could I get that would supply? There really seems to be a gap in the market in the desert but I don't know anyone in that line of business.")
        player<Happy>("Hang on, I know somebody called Aubury, decent fella. I helped him out a while back. Perhaps I could approach him about the idea.")
        npc<Happy>("Oh yes, the rune seller from Varrock. That would be perfect. He's far enough away that he wouldn't end up competing with himself.")
        player["roguetrader_var"] = 2340 // https://chisel.weirdgloop.org/varbs/display?varplayer=540
        player<Quiz>("What do you mean?")
        npc<Talk>("Well the rune market is a limited one, if I were to sell his runes from a location nearby his shop, then I would take part of his market share so he would in fact not benefit from me selling runes.")
        player<Happy>("Ah ha I think I know where this is going. So because you're located far enough away from him you will take up someone else's share in the market and Aubury will make more money as a result.")
        npc<Talk>("Very well anticipated.")
        choice {
            whatsInIt()
            option<Talk>("Ok I'll see what I can do.")
        }
    }
}

suspend fun NPCOption<Player>.fued() {
    player<Talk>("Hi Ali. Not bad. How are you?")
    npc<Talk>("Still selling.")
    npc<Talk>("Hello ${player.name}. How's the adventuring going?")
    player<Talk>("Hi Ali. Not bad. How are you?")
    npc<Talk>("Still selling.")
    choice {
        if (player.questCompleted("the_feud")) {
            option("I would like to have a look at your selection of blackjacks.")
        }
        option("I would like to have a look at your selection of clothes.")
        option("I would like to have a look at your general stock.")
    }
    player<Talk>("Anything interesting?")
    npc<Happy>("For you, always. As a matter of fact I have just acquired a fleet of magic carpets, which operate throughout the Kharidian desert.")
    player<Talk>("Wow, that sounds great, how about your stall?")
    npc<Talk>("Trade is good, but I would like to expand and diversify, but it's proving a little more difficult than I initially thought.")
    player<Uncertain>("How so?")
    npc<Shifty>("I think the other merchants in this town are conspiring against me.")
    player<Talk>("Oh come on! You're just being paranoid now.")
    npc<Talk>("Well maybe I can't get any new suppliers then because I'm Pollnivnian.")
    player<Uncertain>("You're what?")
    npc<RollEyes>("From Pollnivneach.")
    player<Talk>("Maybe I could help you then?")
    npc<Talk>("Perhaps you could.")
    if (player.questCompleted("icthlarins_little_helper")) {
        npc<Talk>("I have an old friend - a cloth merchant Siamun, who lives in Sophanem - Do you know the place?")
        player<Talk>(" Umm..... Images of cats and priests come to mind. Ah yes! There we go, it's the city of the dead South of Pollnivneach isn't it?")
        npc<Talk>("That's the one, well if you could convince him to supply me with some of his merchandise, I would be more than willing to sell them to the masses ...... for a small percentage of course.")
        choice {
            whatsInIt()
            option("Ok I'll get on to it.")
        }
    } else {
        // TODO
    }
}