package content.area.kandarin.ardougne

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Jerico : Script {

    init {
        npcOperate("Talk-to", "jerico") {
            when (questStage("biohazard")) {
                0 -> {
                    player<Happy>("Hello.")
                    npc<Quiz>("Can I help you?")
                    player<Neutral>("Just passing by.")
                }
                1 -> crossingTheWall()
                2, 3 -> distractionIdeas()
                4 -> {
                    player<Happy>("Hello there.")
                    npc<Neutral>("The guards are distracted by the birds, you must go now, quickly traveller.")
                }
                5 -> {
                    player<Happy>("Hello again Jerico.")
                    npc<Quiz>("So you've returned traveller. Did you get what you wanted?")
                    player<Neutral>("Not yet.")
                    npc<Neutral>("Omart will be waiting by the wall, in case you need to cross again.")
                }
                else -> message("Jerico is busy looking for his bird feed.")
            }
        }

        objectOperate("Open", "jericos_cupboard_shut") { (target) ->
            message("You open the cupboard.")
            sound("cupboard_open")
            anim("human_opencupboard")
            target.replace("jericos_cupboard_open", ticks = TimeUnit.MINUTES.toTicks(1))
        }

        objectOperate("Close", "jericos_cupboard_open") { (target) ->
            message("You close the cupboard.")
            sound("cupboard_close")
            anim("human_opencupboard")
            target.remove()
        }

        objectOperate("Search", "jericos_cupboard_open") {
            searchCupboard()
        }
    }

    private suspend fun Player.crossingTheWall() {
        player<Happy>("Hello Jerico.")
        npc<Neutral>("Hello, I've been expecting you. Elena tells me you need to cross the wall.")
        player<Neutral>("That's right.")
        npc<Neutral>("My messenger pigeons help me communicate with friends over the wall.")
        set("biohazard", "spoke_to_jerico")
        npc<Neutral>("I have arranged for two friends to aid you with a rope ladder. Omart is waiting for you at the southern end of the wall.")
        npc<Shifty>("But be careful, if the mourners catch you the punishment will be severe.")
        player<Neutral>("Thanks Jerico.")
    }

    private suspend fun Player.distractionIdeas() {
        player<Happy>("Hello Jerico, I need someway to distract the watch tower, any ideas?")
        npc<Neutral>("Hmmm. Nothing springs to mind.")
        choice {
            shoutAndScream()
            usePigeons()
            beQuiet()
            noIdeas()
        }
    }

    private fun ChoiceOption.shoutAndScream(): Unit = option<Quiz>("Maybe you could shout and scream, and call them away?") {
        npc<Quiz>("So they chase after me?")
        player<Neutral>("Yes. How quickly can you run?")
        npc<Neutral>("No. I don't like this idea.")
    }

    private fun ChoiceOption.usePigeons(): Unit = option<Quiz>("Maybe I could use your messenger pigeons to distract them?") {
        npc<Neutral>("You might have some luck with that idea. The pigeons are around the back of my house if you want to try that.")
        player<Happy>("Ok, maybe I'll give it a go.")
    }

    private fun ChoiceOption.beQuiet(): Unit = option<Quiz>("Maybe if I'm really quiet they won't notice me?") {
        npc<Quiz>("And what stops them from seeing you?")
        player<Quiz>("Well... perhaps I wait till nightfall?")
        npc<Neutral>("There's no time for that.")
    }

    private fun ChoiceOption.noIdeas(): Unit = option<Quiz>("I can't think of anything either.") {
        npc<Neutral>("That's too bad.")
    }

    private suspend fun Player.searchCupboard() {
        item("bird_feed", "The cupboard is full of bird feed.")
        val stage = questStage("biohazard")
        if (stage < 2) {
            player<Neutral>("I guess pigeons really love this stuff.")
            return
        }
        if (stage > 2 || carriesItem("bird_feed")) {
            player<Neutral>("I don't need any more bird feed.")
            return
        }
        addOrDrop("bird_feed")
        player<Confused>("Mmm, bird feed! Now what could I do with that?")
    }
}
