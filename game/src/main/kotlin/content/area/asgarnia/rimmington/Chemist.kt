package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Chemist : Script {

    init {
        npcOperate("Talk-to", "chemist_rimmington") {
            if (questStage("biohazard") in SMUGGLING) {
                choice("What do you want to talk about?") {
                    lamps()
                    quest()
                }
                return@npcOperate
            }
            choice("Do you want to talk about lamps?") {
                option("Yes.") { lampOil() }
                option("No.") { smallTalk() }
            }
        }
    }

    private fun ChoiceOption.lamps(): Unit = option("Lamps.") {
        lampOil()
    }

    private fun ChoiceOption.quest(): Unit = option("Your quest.") {
        if (questStage("biohazard") == 10) {
            closingTime()
        } else {
            moreTouchPaper()
        }
    }

    private suspend fun Player.lampOil() {
        player<Happy>("Hi, I need fuel for a lamp.")
        npc<Neutral>("Hello there, the fuel you need is lamp oil, do you need help making it?")
        choice {
            option<Neutral>("Yes please.") {
                npc<Neutral>("It's really quite simple. You use the small still in here. It's all set up, so there's no fiddling around with dials...")
                npc<Neutral>("Just put ordinary swamp tar in, and then use a lantern or lamp to get the oil out.")
                player<Neutral>("Thanks.")
            }
            option<Neutral>("No thanks.")
        }
    }

    private suspend fun Player.smallTalk() {
        player<Happy>("Hello.")
        npc<Happy>("Oh... hello, how's it going?")
        player<Happy>("Good thanks.")
        npc<Neutral>("Good to hear, sorry but I have a few things to do right now.")
        player<Neutral>("Well I'd better let you get on then.")
    }

    private suspend fun Player.closingTime() {
        npc<Neutral>("Sorry, I'm afraid we're just closing now. You'll have to come back another time.")
        if (!inventory.contains("plague_sample")) {
            return
        }
        choice {
            carryingSample()
            elenasFriend()
        }
    }

    private fun ChoiceOption.carryingSample(): Unit = option("This can't wait, I'm carrying a plague sample.") {
        player<Neutral>("This can't wait, I'm carrying a plague sample that desperately needs analysis.")
        confiscateSample()
    }

    private fun ChoiceOption.elenasFriend(): Unit = option<Happy>("It's ok, I'm Elena's friend.") {
        npc<Neutral>("Oh, well that's different then. Must be pretty important to come all this way.")
        npc<Quiz>("How's everyone doing there anyway? Wasn't there some plague scare?")
        choice {
            touchPaperForSample()
            touchPaperForGuidor()
        }
    }

    private fun ChoiceOption.touchPaperForSample(): Unit = option("I need some more touch paper for this plague sample.") {
        player<Neutral>("That's why I'm here. I need some more touch paper for this plague sample.")
        confiscateSample()
    }

    private fun ChoiceOption.touchPaperForGuidor(): Unit = option("I just need some touch paper for a guy called Guidor.") {
        player<Happy>("Who knows... I just need some touch paper for a guy called Guidor.")
        npc<Neutral>("Guidor? This one's on me then... the poor guy. Sorry for the interrogation.")
        npc<Neutral>("It's just that there's been rumours of a ${if (male) "man" else "woman"} travelling with the plague on ${if (male) "him" else "her"}.")
        npc<Sad>("They're even doing spot checks in Varrock. It's a pharmaceutical disaster!")
        player<Quiz>("Oh right... so am I going to be ok carrying these three vials with me?")
        npc<Confused>("With touch paper as well? You're asking for trouble. You'd better use my errand boys, outside. Give them a vial each.")
        npc<Neutral>("They're not the most reliable people in the world. One's a painter, one's a gambler, and one's a drunk. Still if you pay peanuts you'll get monkeys, right?")
        npc<Neutral>("It's better than entering Varrock with half a laboratory in your napsack.")
        player<Happy>("Ok, thanks for your help. I know Elena appreciates it.")
        npc<Neutral>("Yes well don't stand around here gassing. You'd better hurry if you want to see Guidor... He won't be around for much longer.")
        addOrDrop("touch_paper")
        set("biohazard", "smuggle_chemicals")
    }

    private suspend fun Player.confiscateSample() {
        inventory.remove("plague_sample")
        message("He takes the plague sample from you.")
        npc<Shock>("You idiot! A plague sample should be confined to a lab! I'm taking it off you. I'm afraid it's the only responsible thing to do.")
    }

    private suspend fun Player.moreTouchPaper() {
        player<Neutral>("Hello again.")
        npc<Quiz>("Oh hello, do you need more touch paper?")
        if (inventory.contains("touch_paper")) {
            player<Happy>("No, I just wanted to say hello.")
            npc<Confused>("Oh... ok then... hello.")
            player<Happy>("Hi.")
            return
        }
        player<Happy>("Yes please.")
        npc<Neutral>("Ok, here you go.")
        addOrDrop("touch_paper")
        message("The chemist gives you some touch paper.")
    }

    private companion object {
        val SMUGGLING = 10..14
    }
}
